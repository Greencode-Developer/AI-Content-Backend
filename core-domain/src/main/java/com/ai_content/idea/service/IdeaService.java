package com.ai_content.idea.service;

import com.ai_content.job.command.AiJobCommand;
import com.ai_content.job.domain.Job;
import com.ai_content.job.domain.JobType;
import com.ai_content.job.service.AiJobRepository;
import com.ai_content.job.sqs.AiJobPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IdeaService {

    private final AiJobRepository aiJobRepository;
    private final AiJobPublisher aiJobPublisher;

    @Transactional
    public Long generateIdea(Long userId) {

        Job job = aiJobRepository.createJob(userId, JobType.GENERATE_IDEA);

        AiJobCommand jobSqs = AiJobCommand.create(job.id(),userId);
        // TODO: Transactional outbox pattern
        aiJobPublisher.publish(jobSqs);

        return job.id();
    }
}