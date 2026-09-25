package com.ai_content.brandprofile.service;

import com.ai_content.brandprofile.command.UpdateBrandProfileCommand;
import com.ai_content.brandprofile.domain.BrandProfile;
import com.ai_content.brandprofile.domain.ProfileCompletenessCalculator;
import com.ai_content.common.error.CustomException;
import com.ai_content.common.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BrandProfileService {

    private static final Pattern HEX_COLOR =
            Pattern.compile("^#[0-9A-Fa-f]{6}$");

    private final BrandProfileRepository repository;

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
        if (command == null) {
            throw validationError("Dữ liệu cập nhật không được null");
        }

        BrandProfile current = getByUserId(userId);

        String description = normalizeText(command.description());
        String toneOfVoice = normalizeText(command.toneOfVoice());
        String forbiddenWords = normalizeText(command.forbiddenWords());
        List<String> colors = normalizeColors(command.brandColors());

        int completenessPct = ProfileCompletenessCalculator.calculate(
                description,
                toneOfVoice,
                colors
        );

        BrandProfile updated = new BrandProfile(
                current.id(),
                current.userId(),
                description,
                toneOfVoice,
                forbiddenWords,
                colors,
                completenessPct,
                current.createdAt(),
                current.updatedAt()
        );

        return repository.update(updated)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.BRAND_PROFILE_NOT_FOUND
                ));
    }

    private static String normalizeText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.strip();
        return normalized.isBlank() ? null : normalized;
    }

    private static List<String> normalizeColors(List<String> colors) {
        if (colors == null) {
            throw validationError(
                    "brand_colors không được null; dùng [] để xóa màu"
            );
        }

        Set<String> normalized = new LinkedHashSet<>();

        for (String color : colors) {
            if (color == null) {
                throw validationError(
                        "Mỗi phần tử brand_colors phải là mã màu #RRGGBB"
                );
            }

            String value = color.strip();

            if (!HEX_COLOR.matcher(value).matches()) {
                throw validationError(
                        "Mỗi phần tử brand_colors phải là mã màu #RRGGBB"
                );
            }

            normalized.add(value.toUpperCase(Locale.ROOT));
        }

        return List.copyOf(normalized);
    }

    private static CustomException validationError(String message) {
        return new CustomException(
                ErrorCode.BRAND_PROFILE_VALIDATION_ERROR,
                message
        );
    }
}