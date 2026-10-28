package com.ai_content.presentation;

import com.ai_content.application.ProcessAiJobUseCase;
import com.ai_content.message.AiJobMessage;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiContentJobListener {

    private static final Logger log = LoggerFactory.getLogger(AiContentJobListener.class);

    private final ProcessAiJobUseCase processAiJobUseCase;

    // Visibility must outlast the AI call (read timeout 90s), otherwise SQS redelivers mid-call
    @SqsListener(value = "ai-content-job", messageVisibilitySeconds = "180")
    public void process(AiJobMessage message) {
        log.info("Received AI job {} ({})", message.jobId(), message.type());
        try {
            processAiJobUseCase.execute(message.jobId(), message.type());
        } catch (IllegalArgumentException e) {
            // malformed message → retry is useless, ack to drop it
            log.warn("Dropping invalid AI job message {}: {}", message, e.getMessage());
        }
        // AI errors are handled by marking the job FAILED; anything else (DB down,
        // JobInProgressException) → SQS retry
    }
}
