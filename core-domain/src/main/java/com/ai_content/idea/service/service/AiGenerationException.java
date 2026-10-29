package com.ai_content.idea.service.service;

import lombok.Getter;

@Getter
public class AiGenerationException extends RuntimeException {

    // Langfuse trace of the failed call, if the AI service returned one
    private final String traceId;

    public AiGenerationException(String message) {
        this(message, null, null);
    }

    public AiGenerationException(String message, String traceId) {
        this(message, traceId, null);
    }

    public AiGenerationException(String message, String traceId, Throwable cause) {
        super(message, cause);
        this.traceId = traceId;
    }
}
