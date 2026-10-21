package com.ai_content.producer;

import com.ai_content.message.TrendSignalCollectionMessage;
import com.ai_content.trend_signal.service.TrendSignalCollectionCommand;
import com.ai_content.trend_signal.service.TrendSignalCollectionPublisher;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SqsTrendSignalCollectionPublisher implements TrendSignalCollectionPublisher {
    private final SqsTemplate sqsTemplate;

    @Value("${app.sqs.trend-signal-collect-queue:trend-signal-collect}")
    private String queueName;

    @Override
    public void publish(TrendSignalCollectionCommand command) {
        sqsTemplate.send(to -> to
                .queue(queueName)
                .payload(new TrendSignalCollectionMessage(command.followedChannelId()))
        );
    }
}
