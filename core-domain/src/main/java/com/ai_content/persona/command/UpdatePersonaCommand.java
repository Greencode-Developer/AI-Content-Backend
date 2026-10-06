package com.ai_content.persona.command;

public record UpdatePersonaCommand(
        Long personaId,
        Long userId,
        String name,
        String ageRange,
        String occupation,
        String painPoints,
        String desires,
        String typicalPhrases
) {
    public static UpdatePersonaCommand of(
            Long personaId,
            Long userId,
            String name,
            String ageRange,
            String occupation,
            String painPoints,
            String desires,
            String typicalPhrases
    ) {
        return new UpdatePersonaCommand(personaId, userId, name, ageRange, occupation, painPoints, desires, typicalPhrases);
    }
}
