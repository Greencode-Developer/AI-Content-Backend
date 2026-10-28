package com.ai_content.idea.service.domain;

import com.ai_content.job.domain.AiGenerationMeta;

import java.util.List;

public record AiIdeaGenerationResult(
        List<GeneratedIdea> ideas,
        AiGenerationMeta meta
) {
    public AiIdeaGenerationResult {
        ideas = ideas == null ? List.of() : List.copyOf(ideas);
        meta = meta == null ? AiGenerationMeta.empty() : meta;
    }
}
