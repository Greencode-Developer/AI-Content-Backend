package com.ai_content.presentation;

import com.ai_content.application.CollectTrendSignalUseCase;
import com.ai_content.message.TrendSignalCollectionMessage;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrendSignalCollectionListener {

    private static final Logger log = LoggerFactory.getLogger(TrendSignalCollectionListener.class);

    private final CollectTrendSignalUseCase collectTrendSignalUseCase;

    @SqsListener("${app.sqs.trend-signal-collect-queue:trend-signal-collect}")
    public void process(TrendSignalCollectionMessage message) {
        log.info("Received collection message for channel {}", message.followedChannelId());
        collectTrendSignalUseCase.execute(message.followedChannelId());
    }
}
