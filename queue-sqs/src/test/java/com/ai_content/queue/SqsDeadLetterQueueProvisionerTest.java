package com.ai_content.queue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.CreateQueueResponse;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesResponse;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sqs.model.SetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.SetQueueAttributesResponse;
import software.amazon.awssdk.services.sqs.model.SqsException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SqsDeadLetterQueueProvisionerTest {

    private static final String BASE = "http://localhost:4566/000000000000/";
    private static final String ARN = "arn:aws:sqs:ap-southeast-1:000000000000:";

    @Mock
    SqsAsyncClient sqs;

    private SqsQueueProperties properties(boolean provision) {
        return new SqsQueueProperties("trend-signal-collect", "ai-content-job",
                new SqsQueueProperties.Dlq(provision, "-dlq", 5, 1209600));
    }

    private void stubSqs() {
        lenient().when(sqs.createQueue(any(CreateQueueRequest.class))).thenAnswer(inv -> {
            CreateQueueRequest req = inv.getArgument(0);
            return CompletableFuture.completedFuture(
                    CreateQueueResponse.builder().queueUrl(BASE + req.queueName()).build());
        });
        lenient().when(sqs.getQueueAttributes(any(GetQueueAttributesRequest.class))).thenAnswer(inv -> {
            GetQueueAttributesRequest req = inv.getArgument(0);
            String name = req.queueUrl().substring(BASE.length());
            return CompletableFuture.completedFuture(GetQueueAttributesResponse.builder()
                    .attributes(Map.of(QueueAttributeName.QUEUE_ARN, ARN + name)).build());
        });
        lenient().when(sqs.setQueueAttributes(any(SetQueueAttributesRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(SetQueueAttributesResponse.builder().build()));
    }

    @Test
    void run_createsDlqAndAttachesRedrivePolicyToEveryMainQueue() {
        stubSqs();

        new SqsDeadLetterQueueProvisioner(sqs, properties(true)).run(null);

        ArgumentCaptor<CreateQueueRequest> created = ArgumentCaptor.forClass(CreateQueueRequest.class);
        verify(sqs, atLeastOnce()).createQueue(created.capture());
        assertThat(created.getAllValues()).extracting(CreateQueueRequest::queueName)
                .containsExactly("trend-signal-collect-dlq", "trend-signal-collect",
                        "ai-content-job-dlq", "ai-content-job");
        // created without attributes so an existing queue is returned instead of QueueAlreadyExists
        assertThat(created.getAllValues()).allMatch(req -> req.attributes().isEmpty());

        ArgumentCaptor<SetQueueAttributesRequest> set = ArgumentCaptor.forClass(SetQueueAttributesRequest.class);
        verify(sqs, atLeastOnce()).setQueueAttributes(set.capture());
        List<SetQueueAttributesRequest> calls = set.getAllValues();
        assertThat(calls).hasSize(4);

        assertThat(calls.get(0).queueUrl()).isEqualTo(BASE + "trend-signal-collect-dlq");
        assertThat(calls.get(0).attributes())
                .containsEntry(QueueAttributeName.MESSAGE_RETENTION_PERIOD, "1209600");
        assertThat(calls.get(1).queueUrl()).isEqualTo(BASE + "trend-signal-collect");
        assertThat(calls.get(1).attributes()).containsEntry(QueueAttributeName.REDRIVE_POLICY,
                "{\"deadLetterTargetArn\":\"" + ARN + "trend-signal-collect-dlq\",\"maxReceiveCount\":\"5\"}");

        assertThat(calls.get(3).queueUrl()).isEqualTo(BASE + "ai-content-job");
        assertThat(calls.get(3).attributes()).containsEntry(QueueAttributeName.REDRIVE_POLICY,
                "{\"deadLetterTargetArn\":\"" + ARN + "ai-content-job-dlq\",\"maxReceiveCount\":\"5\"}");
    }

    @Test
    void run_whenDisabled_doesNotCallSqs() {
        new SqsDeadLetterQueueProvisioner(sqs, properties(false)).run(null);

        verifyNoInteractions(sqs);
    }

    @Test
    void run_whenSqsFails_logsAndContinuesWithNextQueue() {
        stubSqs();
        when(sqs.createQueue(any(CreateQueueRequest.class)))
                .thenReturn(CompletableFuture.failedFuture(SqsException.builder().message("unreachable").build()))
                .thenAnswer(inv -> CompletableFuture.completedFuture(CreateQueueResponse.builder()
                        .queueUrl(BASE + ((CreateQueueRequest) inv.getArgument(0)).queueName()).build()));

        assertThatCode(() -> new SqsDeadLetterQueueProvisioner(sqs, properties(true)).run(null))
                .doesNotThrowAnyException();

        // first queue failed at its DLQ; the second queue is still provisioned
        ArgumentCaptor<SetQueueAttributesRequest> set = ArgumentCaptor.forClass(SetQueueAttributesRequest.class);
        verify(sqs, atLeastOnce()).setQueueAttributes(set.capture());
        assertThat(set.getAllValues()).extracting(SetQueueAttributesRequest::queueUrl)
                .containsExactly(BASE + "ai-content-job-dlq", BASE + "ai-content-job");
    }
}
