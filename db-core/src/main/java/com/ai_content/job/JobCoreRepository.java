package com.ai_content.job;

import com.ai_content.job.domain.AiGenerationMeta;
import com.ai_content.job.domain.Job;
import com.ai_content.job.domain.JobStatus;
import com.ai_content.job.domain.JobType;
import com.ai_content.job.service.AiJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JobCoreRepository implements AiJobRepository {
    private final JobJpaRepository jobJpaRepository;

    @Override
    public Job createJob(Long userId, JobType jobType, String input) {
        JobEntity job = JobEntity.create(userId, jobType, input);
        jobJpaRepository.save(job);
        return toDomain(job);
    }

    @Override
    public Optional<Job> findById(Long jobId) {
        return jobJpaRepository.findById(jobId)
                .map(this::toDomain);
    }

    @Override
    @Transactional
    public boolean markProcessing(Long jobId, LocalDateTime staleBefore) {
        return jobJpaRepository.claim(
                jobId, JobStatus.PENDING, JobStatus.PROCESSING, LocalDateTime.now(), staleBefore) == 1;
    }

    @Override
    @Transactional
    public void markCompleted(Long jobId, AiGenerationMeta meta) {
        getJob(jobId).complete(meta.traceId(), meta.model(), meta.promptVersion());
    }

    @Override
    @Transactional
    public void markFailed(Long jobId, String errorMessage, String traceId) {
        getJob(jobId).fail(errorMessage, traceId);
    }

    @Override
    @Transactional
    public int failStuckJobs(LocalDateTime stuckBefore, String errorMessage) {
        return jobJpaRepository.failStuck(
                List.of(JobStatus.PENDING, JobStatus.PROCESSING), JobStatus.FAILED,
                errorMessage, LocalDateTime.now(), stuckBefore);
    }

    private JobEntity getJob(Long jobId) {
        return jobJpaRepository.findById(jobId)
                .orElseThrow(() -> new IllegalStateException("AI job " + jobId + " not found"));
    }

    private Job toDomain(JobEntity job) {
        return Job.of(
                job.getId(),
                job.getUserId(),
                job.getType(),
                job.getStatus(),
                job.getErrorMessage(),
                job.getInput()
        );
    }
}
