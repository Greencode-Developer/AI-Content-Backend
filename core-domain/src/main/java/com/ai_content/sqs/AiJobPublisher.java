package com.ai_content.sqs;

public interface AiJobPublisher {
    void publish(AiJobCommand command);
}
