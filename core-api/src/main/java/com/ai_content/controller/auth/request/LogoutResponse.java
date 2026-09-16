package com.ai_content.controller.auth.request;

import com.ai_content.service.result.LogoutResult;

public record LogoutResponse(
        boolean success,
        String message
) {
    public static LogoutResponse from(LogoutResult result) {
        return new LogoutResponse(result.success(), result.message());
    }
}