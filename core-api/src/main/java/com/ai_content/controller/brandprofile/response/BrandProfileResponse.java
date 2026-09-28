package com.ai_content.controller.brandprofile.response;

import com.ai_content.brandprofile.domain.BrandProfile;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.LocalDateTime;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record BrandProfileResponse(
        Long id,
        String description,
        String toneOfVoice,
        String forbiddenWords,
        List<String> brandColors,
        int completenessPct,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static BrandProfileResponse from(BrandProfile profile) {
        return new BrandProfileResponse(
                profile.id(),
                profile.description(),
                profile.toneOfVoice(),
                profile.forbiddenWords(),
                profile.brandColors(),
                profile.completenessPct(),
                profile.createdAt(),
                profile.updatedAt()
        );
    }
}