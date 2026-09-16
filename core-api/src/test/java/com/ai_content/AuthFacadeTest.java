package com.ai_content;

import com.ai_content.config.jwt.JwtTokenProvider;
import com.ai_content.service.auth.AuthFacade;
import com.ai_content.service.result.LoginResult;
import com.ai_content.user.domain.User;
import com.ai_content.user.domain.UserRole;
import com.ai_content.user.domain.UserStatus;
import com.ai_content.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthFacadeTest {
    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthFacade authFacade;

    private final User user = User.of(
            1L,
            "testuser",
            "test@gmail.com",
            "password",
            UserRole.USER,
            UserStatus.ACTIVE
    );

    @Test
    void loginSuccess() {
        String rawPassword = "user123!";

        when(userService.getByEmail(user.email())).thenReturn(user);
        when(passwordEncoder.matches(rawPassword, user.password())).thenReturn(true);
        when(jwtTokenProvider.createAccessToken(user)).thenReturn("access-token");
        when(jwtTokenProvider.createRefreshToken(user)).thenReturn("refresh-token");
        when(jwtTokenProvider.getAccessTokenValidity()).thenReturn(3_600_000L);
        when(jwtTokenProvider.getRefreshTokenValidity()).thenReturn(604_800_000L);

        LoginResult result = authFacade.login(user.email(), rawPassword);

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        assertThat(result.tokenType()).isEqualTo("Bearer");
        assertThat(result.accessTokenExpiresIn()).isEqualTo(3_600_000L);
        assertThat(result.refreshTokenExpiresIn()).isEqualTo(604_800_000L);
        verify(userService).updateLastLoginAt(user.id());
    }
}
