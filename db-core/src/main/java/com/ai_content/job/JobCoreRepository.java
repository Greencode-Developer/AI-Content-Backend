package com.ai_content.job;

import com.ai_content.job.domain.Job;
import com.ai_content.job.domain.JobType;
import com.ai_content.job.service.AiJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JobCoreRepository implements AiJobRepository {
    private final JobJpaRepository jobJpaRepository;

    @Override
    public Job createJob(Long userId, JobType jobType) {
        JobEntity job = JobEntity.create(userId,jobType);
        jobJpaRepository.save(job);
        return Job.of(job.getId(),job.getUserId(),job.getType(),job.getStatus(),job.getErrorMessage());
    }

    @Override
    public Optional<Job> findById(Long jobId) {
        return jobJpaRepository.findById(jobId)
                .map(job -> Job.of(
                        job.getId(),
                        job.getUserId(),
                        job.getType(),
                        job.getStatus(),
                        job.getErrorMessage()
                ));
    }
}
