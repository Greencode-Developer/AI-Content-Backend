package com.ai_content.job.service;

import com.ai_content.job.domain.AiGenerationMeta;
import com.ai_content.job.domain.Job;
import com.ai_content.job.domain.JobType;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AiJobRepository {
    Job createJob(Long userId, JobType jobType, String input);

    Optional<Job> findById(Long jobId);

    // PENDING → PROCESSING, or re-claims a PROCESSING job started before staleBefore (its worker died).
    // false if the job is missing, finished, or still being processed by another worker
    boolean markProcessing(Long jobId, LocalDateTime staleBefore);

    void markCompleted(Long jobId, AiGenerationMeta meta);

    void markFailed(Long jobId, String errorMessage, String traceId);

    // FAILED for every PENDING/PROCESSING job not started (or not created, if never started) since stuckBefore:
    // its SQS message is gone (DLQ, publish failure) so no worker will pick it up again. Returns the count
    int failStuckJobs(LocalDateTime stuckBefore, String errorMessage);
}
