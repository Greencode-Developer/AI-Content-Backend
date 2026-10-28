package com.ai_content.idea.service.domain;

public record NewIdea(
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
        boolean isExploration
) {

    public static NewIdea fromGenerated(Long userId, Long jobId, IdeaGenerationInput input, GeneratedIdea idea) {
        return new NewIdea(
                userId,
                Platform.FANPAGE,
                idea.title().trim(),
                idea.approachAngle(),
                idea.hookSentence(),
                idea.reason(),
                input.pillarId(),
                jobId,
                input.personaId(),
                input.trendSignalId(),
                input.trendSignalId() != null ? IdeaSource.TREND : IdeaSource.AI_SUGGESTED,
                false
        );
    }
}
