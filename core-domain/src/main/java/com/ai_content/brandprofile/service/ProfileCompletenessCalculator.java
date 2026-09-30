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
        return descriptionScore(description)
                + toneOfVoiceScore(toneOfVoice)
                + colorScore(brandColors);
    }

    private int descriptionScore(String description) {
        int length = textLength(description);

        if (length == 0) {
            return 0;
        }
        if (length < 20) {
            return 10;
        }
        if (length < 50) {
            return 20;
        }
        if (length < 100) {
            return 30;
        }
        return 40;
    }

    private int toneOfVoiceScore(String toneOfVoice) {
        int length = textLength(toneOfVoice);

        if (length == 0) {
            return 0;
        }
        if (length < 5) {
            return 10;
        }
        if (length < 10) {
            return 20;
        }
        if (length < 20) {
            return 30;
        }
        return 40;
    }

    private int colorScore(List<String> brandColors) {
        if (brandColors == null) {
            return 0;
        }

        boolean hasValidColor = brandColors.stream()
                .anyMatch(color -> color != null
                        && HEX_COLOR.matcher(color).matches());

        return hasValidColor ? 20 : 0;
    }

    private int textLength(String value) {
        if (value == null) {
            return 0;
        }

        String normalized = value.strip();
        return normalized.codePointCount(0, normalized.length());
    }
}