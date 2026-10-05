package com.ai_content.message;

public record AiJobMessage(
        Long jobId,
        Long userId,
        String type
) {
}