package com.ai_content.job.command;

import com.ai_content.job.domain.JobType;

public record AiJobCommand(
        Long userId,
        JobType type
) {
    public static AiJobCommand create(Long userId) {
        return new AiJobCommand(
                userId,
                JobType.GENERATE_IDEA
        );
    }
}
