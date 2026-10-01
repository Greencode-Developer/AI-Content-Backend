package com.ai_content.persona.command;

public record CreatePersonaCommand(
    Long userId,
    String name,
    String ageRange,
    String occupation,
    String painPoints,
    String desires,
    String typicalPhrases
){
    public static CreatePersonaCommand of( Long userId,
                                            String name,
                                            String ageRange,
                                            String occupation,
                                            String painPoints,
                                            String desires,
                                            String typicalPhrases){
        return new CreatePersonaCommand(userId, name, ageRange, occupation, painPoints, desires, typicalPhrases);
    }
}
