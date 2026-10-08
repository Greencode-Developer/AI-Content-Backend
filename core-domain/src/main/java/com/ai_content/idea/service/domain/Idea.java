package com.ai_content.idea.service.domain;

import java.time.LocalDateTime;

public record Idea(
        Long id,
        Long userId,
        Platform platform,
        String title,
        String approachAngle,
        String hookSentence,
        String reason,
        Long pillarId,

        Long jobId,
        Long personaId,
        Long trendSignalId,
        IdeaSource source,
        boolean isExploration,
        boolean isUsed,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt
) {

    public static Idea of(
            Long id,
            Long userId,
            Platform platform,
            String title,
            String approachAngle,
            String hookSentence,
            String reason,
            Long pillarId,
            Long jobId,
            Long personaId,
            Long trendSignalId,
            IdeaSource source,
            boolean isExploration,
            boolean isUsed,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime deletedAt
    ) {
        return new Idea(
                id,
                userId,
                platform,
                title,
                approachAngle,
                hookSentence,
                reason,
                pillarId,
                jobId,
                personaId,
                trendSignalId,
                source,
                isExploration,
                isUsed,
                createdAt,
                updatedAt,
                deletedAt
        );
    }

    public boolean isOwner(Long requestUserId) {
        return this.userId.equals(requestUserId);
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public boolean isUsable() {
        return !isDeleted() && !isUsed;
    }
}