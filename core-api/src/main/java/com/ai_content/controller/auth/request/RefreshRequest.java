package com.ai_content.controller.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank
        @Email
        String refreshToken
) {
}