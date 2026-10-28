package com.ai_content.idea.service.domain;

// Snapshot of what is sent to the AI service, stored in ai_jobs.input when the job is created.
// The text fields are never null so the AI service always receives strings.
public record IdeaGenerationInput(
        String topic,
        String brandName,
        String tone,
        String persona,
        String contentPillar,
        Long personaId,
        Long pillarId,
        Long trendSignalId
) {
    public IdeaGenerationInput {
        topic = orEmpty(topic);
        brandName = orEmpty(brandName);
        tone = orEmpty(tone);
        persona = orEmpty(persona);
        contentPillar = orEmpty(contentPillar);
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
