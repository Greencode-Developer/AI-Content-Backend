package com.ai_content.config;
import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import io.awspring.cloud.sqs.listener.acknowledgement.AcknowledgementOrdering;
import io.awspring.cloud.sqs.listener.acknowledgement.handler.AcknowledgementMode;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class SqsConfig {

    @Bean
    public SqsMessageListenerContainerFactory<Object> defaultSqsListenerContainerFactory(
            SqsAsyncClient sqsAsyncClient
    ) {
        return SqsMessageListenerContainerFactory
                .builder()
                .sqsAsyncClient(sqsAsyncClient)
                .configure(options -> options
                        .maxConcurrentMessages(10)
                        .maxMessagesPerPoll(5)
                        .acknowledgementMode(
                                AcknowledgementMode.ON_SUCCESS
                        )
                        .acknowledgementInterval(
                                Duration.ofSeconds(3)
                        )
                        .acknowledgementThreshold(5)
                        .acknowledgementOrdering(
                                AcknowledgementOrdering.ORDERED
                        )
                )
                .build();
    }
}
