package com.ai_content.controller.idea.reponse;

import com.ai_content.idea.service.domain.Idea;
import com.ai_content.idea.service.domain.IdeaSource;
import com.ai_content.idea.service.domain.Platform;

public record IdeaResponse(
        Long id,
        Long userId,
        Platform platform,
        String title,
        String approachAngle,
        String hookSentence,
        String reason,
        Long pillarId,
        Long personaId,
        Long trendSignalId,
        IdeaSource source,
        boolean isExploration,
        boolean isUsed
) {

    public static IdeaResponse from(Idea idea) {
        return new IdeaResponse(
                idea.id(),
                idea.userId(),
                idea.platform(),
                idea.title(),
                idea.approachAngle(),
                idea.hookSentence(),
                idea.reason(),
                idea.pillarId(),
                idea.personaId(),
                idea.trendSignalId(),
                idea.source(),
                idea.isExploration(),
                idea.isUsed()
        );
    }
}