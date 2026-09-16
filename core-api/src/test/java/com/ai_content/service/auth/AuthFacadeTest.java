package com.ai_content.service.auth;

import com.ai_content.common.error.CustomException;
import com.ai_content.common.error.ErrorCode;
import com.ai_content.config.jwt.JwtTokenProvider;
import com.ai_content.config.jwt.TokenPayload;
import com.ai_content.service.auth.AuthFacade;
import com.ai_content.service.result.LoginResult;
import com.ai_content.service.result.RegisterResult;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

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

    @Test
    void invalidPassword() {
        String rawPassword = "wrong-password";
        when(userService.getByEmail(user.email())).thenReturn(user);
        when(passwordEncoder.matches(rawPassword, user.password())).thenReturn(false);

        CustomException exception = assertThrows(
                CustomException.class,
                () -> authFacade.login(user.email(), rawPassword)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_INVALID_PASSWORD);
        verify(userService, never()).updateLastLoginAt(user.id());
    }

    @Test
    void UserNotFound() {
        String email = "missing-email";
        String password = "admin123!";
        CustomException notFound = new CustomException(ErrorCode.USER_NOTFOUND);

        when(userService.getByEmail(email)).thenThrow(notFound);

        CustomException exception = assertThrows(
                CustomException.class,
                () -> authFacade.login(email, password)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOTFOUND);
        verify(userService, never()).updateLastLoginAt(1L);
    }

    @Test
    void refreshSuccess() {
        String refreshToken = "valid-refresh-token";
        TokenPayload payload = new TokenPayload(user.id(), user.role(), "refresh");

        when(jwtTokenProvider.parseRefreshToken(refreshToken)).thenReturn(payload);
        when(userService.getById(user.id())).thenReturn(user);
        when(jwtTokenProvider.createAccessToken(user)).thenReturn("new-access-token");
        when(jwtTokenProvider.createRefreshToken(user)).thenReturn("new-refresh-token");
        when(jwtTokenProvider.getAccessTokenValidity()).thenReturn(3_600_000L);
        when(jwtTokenProvider.getRefreshTokenValidity()).thenReturn(604_800_000L);

        LoginResult result = authFacade.refresh(refreshToken);

        assertThat(result.accessToken()).isEqualTo("new-access-token");
        assertThat(result.refreshToken()).isEqualTo("new-refresh-token");
        assertThat(result.tokenType()).isEqualTo("Bearer");
        assertThat(result.accessTokenExpiresIn()).isEqualTo(3_600_000L);
        assertThat(result.refreshTokenExpiresIn()).isEqualTo(604_800_000L);
    }

    @Test
    void registerSuccess() {
        String fullName = "Test User";
        String email = "test@gmail.com";
        String rawPassword = "user123!";
        String encodedPassword = "$2a$10$encoded-password";

        when(userService.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userService.save(fullName, email, encodedPassword)).thenReturn(user);

        when(jwtTokenProvider.createAccessToken(user)).thenReturn("access-token");
        when(jwtTokenProvider.createRefreshToken(user)).thenReturn("refresh-token");
        when(jwtTokenProvider.getAccessTokenValidity()).thenReturn(3_600_000L);
        when(jwtTokenProvider.getRefreshTokenValidity()).thenReturn(604_800_000L);

        RegisterResult result = authFacade.register(
                fullName,
                email,
                rawPassword
        );

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        assertThat(result.tokenType()).isEqualTo("Bearer");
        assertThat(result.accessTokenExpiresIn()).isEqualTo(3_600_000L);
        assertThat(result.refreshTokenExpiresIn()).isEqualTo(604_800_000L);

        verify(userService).existsByEmail(email);
        verify(passwordEncoder).encode(rawPassword);
        verify(userService).save(fullName, email, encodedPassword);
        verify(userService).updateLastLoginAt(user.id());

        verify(jwtTokenProvider).createAccessToken(user);
        verify(jwtTokenProvider).createRefreshToken(user);
    }

    @Test
    void registerEmailAlreadyExists() {
        String fullName = "Test User";
        String email = "test@gmail.com";
        String password = "user123!";

        when(userService.existsByEmail(email)).thenReturn(true);

        CustomException exception = assertThrows(
                CustomException.class,
                () -> authFacade.register(fullName, email, password)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS);

        verify(userService).existsByEmail(email);

        verify(passwordEncoder, never()).encode(anyString());
        verify(userService, never()).save(anyString(), anyString(), anyString());
        verify(userService, never()).updateLastLoginAt(anyLong());

        verify(jwtTokenProvider, never()).createAccessToken(any());
        verify(jwtTokenProvider, never()).createRefreshToken(any());
    }

    @Test
    void registerShouldHashPasswordBeforeSaving() {
        String fullName = "Test User";
        String email = "test@gmail.com";
        String rawPassword = "user123!";
        String encodedPassword = "$2a$10$hashed-password";

        when(userService.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userService.save(fullName, email, encodedPassword)).thenReturn(user);

        when(jwtTokenProvider.createAccessToken(user)).thenReturn("access-token");
        when(jwtTokenProvider.createRefreshToken(user)).thenReturn("refresh-token");
        when(jwtTokenProvider.getAccessTokenValidity()).thenReturn(3_600_000L);
        when(jwtTokenProvider.getRefreshTokenValidity()).thenReturn(604_800_000L);

        authFacade.register(fullName, email, rawPassword);

        verify(passwordEncoder).encode(rawPassword);

        verify(userService).save(
                fullName,
                email,
                encodedPassword
        );

        verify(userService, never()).save(
                fullName,
                email,
                rawPassword
        );
    }
}
