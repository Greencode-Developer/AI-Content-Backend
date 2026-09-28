package com.ai_content.brandprofile.service;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class ProfileCompletenessCalculator {
    private static final Pattern HEX_COLOR =
            Pattern.compile("^#[0-9A-Fa-f]{6}$");

    public int calculate(
            String description,
            String toneOfVoice,
            List<String> brandColors
    ) {
        int score = 0;

        if (hasText(description)) {
            score += 40;
        }

        if (hasText(toneOfVoice)) {
            score += 40;
        }

        if (brandColors != null
                && brandColors.stream().anyMatch(ProfileCompletenessCalculator::isValidColor)) {
            score += 20;
        }

        return score;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static boolean isValidColor(String value) {
        return value != null && HEX_COLOR.matcher(value).matches();
    }

}
