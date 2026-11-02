package com.ai_content.application;

import com.ai_content.idea.service.service.IdeaGenerationService;
import com.ai_content.job.domain.JobType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProcessAiJobUseCase {

    private final IdeaGenerationService ideaGenerationService;

    public void execute(Long jobId, JobType type) {
        if (jobId == null || type == null) {
            throw new IllegalArgumentException("jobId and type are required");
        }

        switch (type) {
            case GENERATE_IDEA -> ideaGenerationService.process(jobId);
        }
    }
}