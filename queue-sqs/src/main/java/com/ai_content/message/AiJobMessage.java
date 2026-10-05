package com.ai_content.message;

import com.ai_content.job.domain.JobType;

public record AiJobMessage(
        Long jobId,
        Long userId,
        JobType type
) {
}