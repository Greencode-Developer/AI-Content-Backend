package com.ai_content.job.sqs;

import com.ai_content.job.command.AiJobCommand;

public interface AiJobPublisher {
    void publish(AiJobCommand command);
}
