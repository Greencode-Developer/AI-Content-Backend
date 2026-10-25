package com.ai_content.queue;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

/**
 * Queue names under app.sqs and their dead-letter queue settings.
 * Defaults match the names used by the publishers and listeners, so a module
 * that does not import sqs.yml (ai-worker) still gets them.
 */
@ConfigurationProperties(prefix = "app.sqs")
public record SqsQueueProperties(
        @DefaultValue("trend-signal-collect") String trendSignalCollectQueue,
        @DefaultValue("ai-content-job") String aiContentJobQueue,
        @DefaultValue Dlq dlq
) {

    public List<String> mainQueues() {
        return List.of(trendSignalCollectQueue, aiContentJobQueue);
    }

    /**
     * @param provision        create the DLQs and attach the redrive policy on startup.
     *                         Turn off where the infrastructure owns the queues (production).
     * @param suffix           DLQ name = main queue name + suffix
     * @param maxReceiveCount  receives without ack before SQS moves a message to the DLQ
     * @param retentionSeconds how long the DLQ keeps messages (max 14 days). Must outlive the
     *                         main queue retention, because the age counts from the first enqueue.
     */
    public record Dlq(
            @DefaultValue("true") boolean provision,
            @DefaultValue("-dlq") String suffix,
            @DefaultValue("5") int maxReceiveCount,
            @DefaultValue("1209600") int retentionSeconds
    ) {
        public String nameFor(String queue) {
            return queue + suffix;
        }
    }
}
