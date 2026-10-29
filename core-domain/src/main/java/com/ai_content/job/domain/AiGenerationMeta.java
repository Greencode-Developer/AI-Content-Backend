package com.ai_content.job.domain;

// Links a job to its Langfuse trace and the prompt/model that produced the result.
// All fields are nullable: the AI service may not return them.
public record AiGenerationMeta(
        String traceId,
        String model,
        String promptVersion
) {
    public static AiGenerationMeta empty() {
        return new AiGenerationMeta(null, null, null);
    }
}
