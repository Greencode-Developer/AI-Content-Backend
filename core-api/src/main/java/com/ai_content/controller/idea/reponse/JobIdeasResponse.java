package com.ai_content.controller.idea.reponse;

import com.ai_content.idea.service.domain.JobIdeas;
import com.ai_content.job.domain.JobStatus;

import java.util.List;

// status lets the client tell "still running" (PENDING/PROCESSING) from "failed" and "done"
public record JobIdeasResponse(
        Long jobId,
        JobStatus status,
        String errorMessage,
        List<IdeaResponse> ideas
) {
    public static JobIdeasResponse from(JobIdeas jobIdeas) {
        return new JobIdeasResponse(
                jobIdeas.job().id(),
                jobIdeas.job().status(),
                jobIdeas.job().errorMessage(),
                jobIdeas.ideas().stream()
                        .map(IdeaResponse::from)
                        .toList()
        );
    }
}
