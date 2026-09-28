package com.ai_content.gemini;

import com.ai_content.ai.AiGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "ai",
        name = "provider",
        havingValue = "gemini"
)
public class GeminiAiGenerator implements AiGenerator {
}
