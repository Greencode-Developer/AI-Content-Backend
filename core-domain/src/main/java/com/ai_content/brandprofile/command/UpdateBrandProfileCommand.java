package com.ai_content.brandprofile.command;

import java.util.List;

public record UpdateBrandProfileCommand(
        String description,
        String toneOfVoice,
        String forbiddenWords,
        List<String> brandColors
) {
}