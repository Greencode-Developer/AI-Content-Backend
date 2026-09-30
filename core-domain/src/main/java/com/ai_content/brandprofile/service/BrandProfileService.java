package com.ai_content.brandprofile.service;

import com.ai_content.brandprofile.command.UpdateBrandProfileCommand;
import com.ai_content.brandprofile.domain.BrandProfile;
import com.ai_content.common.error.CustomException;
import com.ai_content.common.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BrandProfileService {

    private final BrandProfileRepository repository;

    private final ProfileCompletenessCalculator profileCompletenessCalculator;

    public BrandProfile getByUserId(Long userId) {
        return repository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.BRAND_PROFILE_NOT_FOUND
                ));
    }

    @Transactional
    public BrandProfile createEmpty(Long userId) {
        return repository.createEmpty(userId);
    }

    @Transactional
    public BrandProfile update(
            Long userId,
            UpdateBrandProfileCommand command
    ) {

        BrandProfile current = getByUserId(userId);

        int completenessPct = profileCompletenessCalculator.calculate(
            command.description(),
            command.toneOfVoice(),
            command.brandColors()
        );

        BrandProfile updated = new BrandProfile(
                current.id(),
                current.userId(),
                command.description(),
                command.toneOfVoice(),
                command.forbiddenWords(),
                command.brandColors(),
                completenessPct,
                current.createdAt(),
                current.updatedAt()
        );

        return repository.update(updated)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.BRAND_PROFILE_NOT_FOUND
                ));
    }
}