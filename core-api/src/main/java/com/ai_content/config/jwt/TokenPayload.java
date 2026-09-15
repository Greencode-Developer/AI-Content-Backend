package com.ai_content.config.jwt;

import com.ai_content.user.domain.UserRole;

public record TokenPayload(Long userId, UserRole roleUser, String tokenType) {
}
