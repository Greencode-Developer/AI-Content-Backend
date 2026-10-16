package com.ai_content.config;

import com.ai_content.job.command.AiJobCommand;
import com.ai_content.job.sqs.AiJobPublisher;
import org.springframework.stereotype.Component;

@Component
public class NoOpAiJobPublisher implements AiJobPublisher {
    @Override
    public void publish(AiJobCommand command) {
        // empty - schedule not use SQS
    }
}
