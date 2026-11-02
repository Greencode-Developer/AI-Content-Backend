package com.ai_content.idea.service.service;

import com.ai_content.idea.service.domain.AiIdeaGenerationResult;
import com.ai_content.idea.service.domain.IdeaGenerationInput;
import com.ai_content.idea.service.domain.NewIdea;
import com.ai_content.job.domain.Job;
import com.ai_content.job.service.AiJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Separate bean so the save runs in its own transaction (no self-invocation from IdeaGenerationService)
@Component
@RequiredArgsConstructor
public class IdeaGenerationResultWriter {

    private final IdeaRepository ideaRepository;
    private final AiJobRepository aiJobRepository;

    // Ideas and the COMPLETED status are saved together or not at all
    @Transactional
    public void save(Job job, IdeaGenerationInput input, AiIdeaGenerationResult result) {
        List<NewIdea> ideas = result.ideas().stream()
                .map(idea -> NewIdea.fromGenerated(job.userId(), job.id(), input, idea))
                .toList();

        ideaRepository.saveAll(ideas);
        aiJobRepository.markCompleted(job.id(), result.meta());
    }
}
