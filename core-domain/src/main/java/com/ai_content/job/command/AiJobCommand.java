package com.ai_content.job.command;

import com.ai_content.job.domain.JobType;

public record AiJobCommand(
        Long jobId,
        Long userId,
        JobType type
) {
    public static AiJobCommand create(Long jobId,Long userId) {
        return new AiJobCommand(
                jobId,
                userId,
                JobType.GENERATE_IDEA
        );
    }
}
