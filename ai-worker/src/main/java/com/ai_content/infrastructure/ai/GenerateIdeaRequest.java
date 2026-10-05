package com.ai_content.infrastructure.ai;

public record GenerateIdeaRequest(
        String topic,
        String brandName,
        String tone,
        String persona,
        String contentPillar
) {}