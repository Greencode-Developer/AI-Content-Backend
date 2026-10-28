package com.ai_content.queue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class SqsQueuePropertiesTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(SqsQueueConfig.class);

    @Test
    void defaults_whenNothingConfigured() {
        runner.run(context -> {
            SqsQueueProperties props = context.getBean(SqsQueueProperties.class);
            assertThat(props.mainQueues()).containsExactly("trend-signal-collect", "ai-content-job");
            assertThat(props.dlq().provision()).isTrue();
            assertThat(props.dlq().maxReceiveCount()).isEqualTo(5);
            assertThat(props.dlq().retentionSeconds()).isEqualTo(1209600);
            assertThat(props.dlq().nameFor("ai-content-job")).isEqualTo("ai-content-job-dlq");
        });
    }

    @Test
    void overrides_fromProperties() {
        runner.withPropertyValues(
                "app.sqs.ai-content-job-queue=jobs",
                "app.sqs.dlq.provision=false",
                "app.sqs.dlq.max-receive-count=3"
        ).run(context -> {
            SqsQueueProperties props = context.getBean(SqsQueueProperties.class);
            assertThat(props.aiContentJobQueue()).isEqualTo("jobs");
            assertThat(props.dlq().provision()).isFalse();
            assertThat(props.dlq().maxReceiveCount()).isEqualTo(3);
        });
    }
}
