package com.ai_content.idea.service.domain;

import com.ai_content.job.domain.Job;

import java.util.List;

public record JobIdeas(
        Job job,
        List<Idea> ideas
) {
}
