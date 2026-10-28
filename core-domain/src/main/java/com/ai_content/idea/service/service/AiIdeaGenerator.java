package com.ai_content.idea.service.service;

import com.ai_content.idea.service.domain.AiIdeaGenerationResult;
import com.ai_content.idea.service.domain.IdeaGenerationInput;

// Output port to the AI service. Implementations wrap every failure in AiGenerationException.
public interface AiIdeaGenerator {
    AiIdeaGenerationResult generate(Long jobId, Long userId, IdeaGenerationInput input);
}
