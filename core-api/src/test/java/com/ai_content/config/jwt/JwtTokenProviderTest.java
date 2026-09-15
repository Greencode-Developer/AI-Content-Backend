package com.ai_content.config.jwt;

import com.ai_content.common.error.CustomException;
import com.ai_content.common.error.ErrorCode;
import com.ai_content.config.jwt.JwtProperties;
import com.ai_content.config.jwt.JwtTokenProvider;
import com.ai_content.config.jwt.TokenPayload;
import com.ai_content.user.domain.UserRole;
import com.ai_content.user.domain.User;
import com.ai_content.user.domain.UserStatus;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JwtTokenProviderTest {
    private static final String SECRET_KEY = "this-is-a-sufficiently-long-test-secret-key-12345";
    private static final long ACCESS_TOKEN_VALIDITY = 1_000L;
    private static final long REFRESH_TOKEN_VALIDITY = 2_000L;
    private static final long SHORT_LIVED_ACCESS_TOKEN_VALIDITY = 10L;

    private final JwtTokenProvider tokenProvider =
            new JwtTokenProvider(JwtProperties.of(SECRET_KEY, ACCESS_TOKEN_VALIDITY, REFRESH_TOKEN_VALIDITY));

    private final JwtTokenProvider shortLivedTokenProvider =
            new JwtTokenProvider(JwtProperties.of(SECRET_KEY, SHORT_LIVED_ACCESS_TOKEN_VALIDITY, REFRESH_TOKEN_VALIDITY));
    private final User user = User.of(
            1L,
            "testuser",
            "test@gmail.com",
            "password",
            UserRole.USER,
            UserStatus.ACTIVE
    );
    @Test
    void createAndParseAccessToken() {
        String token = tokenProvider.createAccessToken(user);

        TokenPayload payload = tokenProvider.parseAccessToken(token);

        assertThat(payload.userId()).isEqualTo(user.id());
        assertThat(payload.roleUser()).isEqualTo(user.role());
        assertThat(payload.tokenType()).isEqualTo("access");
    }

    @Test
    void parseRefreshTokenAsAccessTokenShouldFail() {
        String refreshToken = tokenProvider.createRefreshToken(user);

        CustomException exception = assertThrows(
                CustomException.class,
                () -> tokenProvider.parseAccessToken(refreshToken)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_TOKEN_INVALID);
    }

    @Test
    void expiredTokenShouldFail() throws InterruptedException {
        String token = shortLivedTokenProvider.createAccessToken(user);
        Thread.sleep(30);

        CustomException exception = assertThrows(
                CustomException.class,
                () -> shortLivedTokenProvider.parseAccessToken(token)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_TOKEN_EXPIRED);
    }

}
