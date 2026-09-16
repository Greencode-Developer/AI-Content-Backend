package com.ai_content.service.auth;

import com.ai_content.common.error.CustomException;
import com.ai_content.common.error.ErrorCode;
import com.ai_content.config.jwt.JwtTokenProvider;
import com.ai_content.service.result.LoginResult;
import com.ai_content.user.domain.User;
import com.ai_content.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthFacade {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public LoginResult login(String email, String password) {
        User user = userService.getByEmail(email);

        if (!passwordEncoder.matches(password, user.password())) {
            throw new CustomException(ErrorCode.USER_INVALID_PASSWORD);
        }

        userService.updateLastLoginAt(user.id());

        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user);

        return new LoginResult(
                accessToken,
                refreshToken,
                "Bearer",
                jwtTokenProvider.getAccessTokenValidity(),
                jwtTokenProvider.getRefreshTokenValidity()
        );
    }

}