package com.ai_content.config.jwt;

import com.ai_content.user.domain.Role;
import com.ai_content.user.domain.User;

public record TokenPayload(Long userId, Role roleUser, String tokenType) {
}
