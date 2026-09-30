package com.ai_content.controller.brandprofile.response;

import com.ai_content.brandprofile.domain.BrandProfile;

import java.time.LocalDateTime;
import java.util.List;

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