package com.ai_content.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sqs.model.SetQueueAttributesRequest;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/*
 * Gives every main queue a dead-letter queue:
 *   <queue>-dlq is created, and the main queue gets RedrivePolicy {deadLetterTargetArn, maxReceiveCount}.
 * SQS then moves a message to the DLQ by itself after maxReceiveCount receives without ack
 *
 * Idempotent: queues are created without attributes (CreateQueue returns the existing URL)
 * and attributes are applied with SetQueueAttributes, so it is safe on every startup and in
 * every app that includes queue-sqs. Failures are only logged: the app must start without SQS.
 */
@Component
public class SqsDeadLetterQueueProvisioner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SqsDeadLetterQueueProvisioner.class);

    private static final long TIMEOUT_SECONDS = 10;

    private final SqsAsyncClient sqsAsyncClient;
    private final SqsQueueProperties properties;

    public SqsDeadLetterQueueProvisioner(SqsAsyncClient sqsAsyncClient, SqsQueueProperties properties) {
        this.sqsAsyncClient = sqsAsyncClient;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.dlq().provision()) {
            log.info("DLQ provisioning disabled (app.sqs.dlq.provision=false)");
            return;
        }
        for (String queue : properties.mainQueues()) {
            try {
                provision(queue);
            } catch (RuntimeException e) {
                log.warn("Could not set up DLQ for queue {}: {}", queue, e.getMessage());
            }
        }
    }

    void provision(String queue) {
        SqsQueueProperties.Dlq dlq = properties.dlq();
        String dlqName = dlq.nameFor(queue);

        String dlqUrl = createQueue(dlqName);
        setAttributes(dlqUrl, Map.of(
                QueueAttributeName.MESSAGE_RETENTION_PERIOD, String.valueOf(dlq.retentionSeconds())));
        String dlqArn = await(sqsAsyncClient.getQueueAttributes(GetQueueAttributesRequest.builder()
                .queueUrl(dlqUrl)
                .attributeNames(QueueAttributeName.QUEUE_ARN)
                .build()))
                .attributes().get(QueueAttributeName.QUEUE_ARN);

        String queueUrl = createQueue(queue);
        setAttributes(queueUrl, Map.of(
                QueueAttributeName.REDRIVE_POLICY, redrivePolicy(dlqArn, dlq.maxReceiveCount())));

        log.info("Queue {} → DLQ {} after {} receives", queue, dlqName, dlq.maxReceiveCount());
    }

    static String redrivePolicy(String dlqArn, int maxReceiveCount) {
        return "{\"deadLetterTargetArn\":\"" + dlqArn + "\",\"maxReceiveCount\":\"" + maxReceiveCount + "\"}";
    }

    private String createQueue(String name) {
        return await(sqsAsyncClient.createQueue(CreateQueueRequest.builder().queueName(name).build()))
                .queueUrl();
    }

    private void setAttributes(String queueUrl, Map<QueueAttributeName, String> attributes) {
        await(sqsAsyncClient.setQueueAttributes(SetQueueAttributesRequest.builder()
                .queueUrl(queueUrl)
                .attributes(attributes)
                .build()));
    }

    private <T> T await(CompletableFuture<T> future) {
        return future.orTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS).join();
    }
}
