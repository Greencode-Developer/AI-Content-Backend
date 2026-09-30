package com.ai_content.controller.brandprofile.request;

import com.ai_content.brandprofile.command.UpdateBrandProfileCommand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta .validation.constraints.Pattern;

import java.util.List;

public record UpdateBrandProfileRequest(

    String description,
    @NotBlank
    String toneOfVoice,
    String forbiddenWords,

    @NotNull
    List<
        @NotBlank
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
        String
    > brandColors
) {
    public UpdateBrandProfileCommand toCommand() {
        return new UpdateBrandProfileCommand(
            description,
            toneOfVoice,
            forbiddenWords,
            brandColors
        );
    }
}