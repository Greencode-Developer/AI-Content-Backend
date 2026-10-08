package com.ai_content.job.service;

import com.ai_content.job.command.AiJobCommand;
import com.ai_content.job.domain.Job;
import com.ai_content.job.domain.JobType;

import java.util.Optional;

public interface AiJobRepository {
    Job createJob(Long userId, JobType jobType);

    Optional<Job> findById(Long jobId);
}
