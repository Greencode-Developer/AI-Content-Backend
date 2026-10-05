package com.ai_content.job.domain;

public record Job(
        Long id,
        Long userId,
        JobType type,
        JobStatus status,
        String errorMessage
) {

    public static Job of(
            Long id,
            Long userId,
            JobType type,
            JobStatus status,
            String errorMessage
    ) {
        return new Job(
                id,
                userId,
                type,
                status,
                errorMessage
        );
    }


    public boolean isPending() {
        return this.status == JobStatus.PENDING;
    }

    public boolean isProcessing() {
        return this.status == JobStatus.PROCESSING;
    }

    public boolean isCompleted() {
        return this.status == JobStatus.COMPLETED;
    }

    public boolean isFailed() {
        return this.status == JobStatus.FAILED;
    }
}
