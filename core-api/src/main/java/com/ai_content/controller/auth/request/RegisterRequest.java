package com.ai_content.controller.auth.request;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank
        String fullName,

        @NotBlank
        String email,

        @NotBlank
        String password
) {
}