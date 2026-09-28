package com.ai_content.sqs;

public record AiJobCommand(
        Long jobId,
        Long userId,
        String type
) {
}
