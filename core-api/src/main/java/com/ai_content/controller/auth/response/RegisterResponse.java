package com.ai_content.controller.auth.response;

import com.ai_content.service.result.RegisterResult;

public record RegisterResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn
) {
    public static RegisterResponse from(RegisterResult result) {
        return new RegisterResponse(
                result.accessToken(),
                result.refreshToken(),
                result.tokenType(),
                result.accessTokenExpiresIn(),
                result.refreshTokenExpiresIn()
        );
    }
}