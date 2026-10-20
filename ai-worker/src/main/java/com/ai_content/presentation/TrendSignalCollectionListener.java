package com.ai_content.presentation;

import com.ai_content.application.CollectTrendSignalUseCase;
import com.ai_content.message.TrendSignalCollectionMessage;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class TrendSignalCollectionListener {

    private static final Logger log = LoggerFactory.getLogger(TrendSignalCollectionListener.class);

    // 4xx nhưng là lỗi tạm thời → vẫn cần SQS retry
    private static final Set<HttpStatus> RETRYABLE_CLIENT_ERRORS = Set.of(
            HttpStatus.REQUEST_TIMEOUT,
            HttpStatus.TOO_MANY_REQUESTS
    );

    private final CollectTrendSignalUseCase collectTrendSignalUseCase;

    @SqsListener("${app.sqs.trend-signal-collect-queue:trend-signal-collect}")
    public void process(TrendSignalCollectionMessage message) {
        log.info("Received collection message for channel {}", message.followedChannelId());
        try {
            collectTrendSignalUseCase.execute(message.followedChannelId());
        } catch (HttpClientErrorException e) {
            if (RETRYABLE_CLIENT_ERRORS.contains(HttpStatus.resolve(e.getStatusCode().value()))) {
                throw e;
            }
            drop(message, e);
        } catch (IllegalArgumentException e) {
            drop(message, e);
        }
        // lỗi khác (timeout, 5xx, DB) → ném ra để SQS retry
    }

    // lỗi vĩnh viễn (4xx, message sai) → retry vô ích, ack để bỏ message
    private void drop(TrendSignalCollectionMessage message, Exception e) {
        log.warn("Channel {} - non-retryable error, dropping message: {}",
                message.followedChannelId(), e.getMessage());
    }
}
