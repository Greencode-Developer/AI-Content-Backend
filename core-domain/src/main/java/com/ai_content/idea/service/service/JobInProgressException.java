package com.ai_content.idea.service.service;

// The job is PROCESSING on another worker (or one that died recently).
// Thrown so SQS redelivers the message later instead of acking it and leaving the job stuck.
public class JobInProgressException extends RuntimeException {

    public JobInProgressException(Long jobId) {
        super("AI job " + jobId + " is still being processed");
    }
}
