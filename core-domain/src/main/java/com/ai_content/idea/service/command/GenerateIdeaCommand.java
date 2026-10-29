package com.ai_content.idea.service.command;

public record GenerateIdeaCommand(
        String topic,
        Long personaId,
        Long pillarId,
        Long trendSignalId
) {
    public static GenerateIdeaCommand empty() {
        return new GenerateIdeaCommand(null, null, null, null);
    }
}
