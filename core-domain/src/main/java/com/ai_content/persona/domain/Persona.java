package com.ai_content.persona.domain;

public record Persona(
        Long id,
        Long userId,
        String name,
        String ageRange,
        String occupation,
        String painPoints,
        String desires,
        String typicalPhrases,
        PersonaStatus status
) {
    public static Persona of(
            Long id,
            Long userId,
            String name,
            String ageRange,
            String occupation,
            String painPoints,
            String desires,
            String typicalPhrases,
            PersonaStatus status
    ) {
        return new Persona(
                id,
                userId,
                name,
                ageRange,
                occupation,
                painPoints,
                desires,
                typicalPhrases,
                status
        );
    }

    public boolean isActive() {
        return this.status == PersonaStatus.ACTIVE;
    }
}