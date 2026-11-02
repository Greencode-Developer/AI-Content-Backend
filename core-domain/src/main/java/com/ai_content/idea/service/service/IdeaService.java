package com.ai_content.idea.service.service;

import com.ai_content.common.error.CustomException;
import com.ai_content.common.error.ErrorCode;
import com.ai_content.idea.service.command.GenerateIdeaCommand;
import com.ai_content.idea.service.domain.IdeaGenerationInput;
import com.ai_content.idea.service.domain.JobIdeas;
import com.ai_content.job.command.AiJobCommand;
import com.ai_content.job.domain.Job;
import com.ai_content.job.domain.JobType;
import com.ai_content.job.service.AiJobRepository;
import com.ai_content.job.sqs.AiJobPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IdeaService {

    private final AiJobRepository aiJobRepository;
    private final AiJobPublisher aiJobPublisher;
    private final IdeaRepository ideaRepository;
    private final IdeaGenerationInputAssembler inputAssembler;
    private final IdeaGenerationInputCodec inputCodec;

    @Transactional
    public Long generateIdea(Long userId, GenerateIdeaCommand command) {
        IdeaGenerationInput input = inputAssembler.assemble(userId, command);

        Job job = aiJobRepository.createJob(userId, JobType.GENERATE_IDEA, inputCodec.toJson(input));

        AiJobCommand jobSqs = AiJobCommand.create(job.id(),userId);
        // TODO: Transactional outbox pattern (a publish failure after commit leaves the job PENDING)
        publishAfterCommit(jobSqs);

        return job.id();
    }

    // The worker can receive the message within milliseconds. Publishing inside the transaction
    // lets it look up a job row that is not committed yet, so it skips the job and it stays PENDING.
    private void publishAfterCommit(AiJobCommand command) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            aiJobPublisher.publish(command);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                aiJobPublisher.publish(command);
            }
        });
    }

    public JobIdeas getJobIdeas(Long userId, Long jobId) {
        Job job = aiJobRepository.findById(jobId)
                .orElseThrow(() -> new CustomException(ErrorCode.AI_JOB_NOT_FOUND));

        if (!job.isOwner(userId)) {
            throw new CustomException(ErrorCode.AI_JOB_ACCESS_DENIED);
        }

        return new JobIdeas(job, ideaRepository.findAllByJobId(jobId));
    }
}
