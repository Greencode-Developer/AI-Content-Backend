package com.ai_content.brandprofile.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record BrandProfile(
        UUID id,
        Long userId,
        String description,
        String toneOfVoice,
        String forbiddenWords,
        List<String> brandColors,
        int completenessPct,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public BrandProfile {
        brandColors = brandColors == null
                ? List.of()
                : List.copyOf(brandColors);
    }
}