package com.ai_content.config;

import com.ai_content.idea.service.service.AiIdeaGenerator;
import com.ai_content.idea.service.service.IdeaGenerationInputCodec;
import com.ai_content.idea.service.service.IdeaGenerationResultWriter;
import com.ai_content.idea.service.service.IdeaGenerationService;
import com.ai_content.job.service.AiJobRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class AiJobConfig {

    @Bean
    public IdeaGenerationService ideaGenerationService(
            AiJobRepository aiJobRepository,
            AiIdeaGenerator aiIdeaGenerator,
            IdeaGenerationInputCodec inputCodec,
            IdeaGenerationResultWriter resultWriter,
            @Value("${ai.idea.processing-timeout:5m}") Duration processingTimeout
    ) {
        return new IdeaGenerationService(aiJobRepository, aiIdeaGenerator, inputCodec, resultWriter, processingTimeout);
    }
}
