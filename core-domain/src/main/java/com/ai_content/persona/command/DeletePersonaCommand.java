package com.ai_content.persona.command;

public record DeletePersonaCommand(
        Long personaId,
        Long userId
) {
    public static DeletePersonaCommand of(Long personaId, Long userId) {
        return new DeletePersonaCommand(personaId, userId);
    }
}
