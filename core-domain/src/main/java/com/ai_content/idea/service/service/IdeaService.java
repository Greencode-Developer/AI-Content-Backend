package com.ai_content.idea.service.service;

import com.ai_content.common.error.CustomException;
import com.ai_content.common.error.ErrorCode;
import com.ai_content.idea.service.domain.Idea;
import com.ai_content.job.command.AiJobCommand;
import com.ai_content.job.domain.Job;
import com.ai_content.job.domain.JobType;
import com.ai_content.job.service.AiJobRepository;
import com.ai_content.job.sqs.AiJobPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IdeaService {

    private final AiJobRepository aiJobRepository;
    private final AiJobPublisher aiJobPublisher;
    private final IdeaRepository ideaRepository;

    @Transactional
    public Long generateIdea(Long userId) {

        Job job = aiJobRepository.createJob(userId, JobType.GENERATE_IDEA);

        AiJobCommand jobSqs = AiJobCommand.create(job.id(),userId);
        // TODO: Transactional outbox pattern
        aiJobPublisher.publish(jobSqs);

        return job.id();
    }
    public List<Idea> getIdeasByJobId(Long userId, Long jobId) {
        Job job = aiJobRepository.findById(jobId)
                .orElseThrow(() -> new CustomException(ErrorCode.AI_JOB_NOT_FOUND));

        if (!job.isOwner(userId)) {
            throw new CustomException(ErrorCode.AI_JOB_ACCESS_DENIED);
        }

        return ideaRepository.findAllByJobId(jobId);
    }
}