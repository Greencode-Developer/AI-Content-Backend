package com.ai_content.controller.idea.request;

import com.ai_content.idea.service.command.GenerateIdeaCommand;
import jakarta.validation.constraints.Size;

/**
 * Optional body of POST /api/v1/ideas/generate. Every field is optional;
 * persona / pillar / trend signal must belong to the current user.
 */
public record GenerateIdeaRequest(
        @Size(max = 500, message = "topic must be at most 500 characters") String topic,
        Long personaId,
        Long pillarId,
        Long trendSignalId
) {
    public GenerateIdeaCommand toCommand() {
        return new GenerateIdeaCommand(topic, personaId, pillarId, trendSignalId);
    }
}
