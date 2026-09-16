package com.ai_content.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.ai_content.common.error.ErrorCode;
import com.ai_content.config.jwt.JwtTokenProvider;
import com.ai_content.user.UserEntity;
import com.ai_content.user.UserJpaRepository;
import com.ai_content.user.domain.User;
import com.ai_content.user.domain.UserRole;
import com.ai_content.user.domain.UserStatus;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.Test;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserJpaRepository userJpaRepository;

    private final User user = User.of(
            1L,
            "testuser",
            "test@gmail.com",
            "password",
            UserRole.USER,
            UserStatus.ACTIVE
    );

    private final User usermissing = User.of(
            999_999L,
            "missing-user",
            "test@gmail.com",
            "password",
            UserRole.USER,
            UserStatus.ACTIVE
    );

    UserEntity userEntity = UserEntity.createUser("","","");


    @Test
    void accessWithoutTokenShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/users/test"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.data.errorCode").value(ErrorCode.USER_UNAUTHORIZED.getCode()));
    }

    @Test
    void accessWithAdminTokenShouldReturn200() throws Exception {
        userJpaRepository.save(userEntity);

        String accessToken = jwtTokenProvider.createAccessToken(user);
        mockMvc.perform(
                        get("/api/v1/users/test")
                                .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("user authenticated"));
    }
    @Test
    void accessWithMissingAdminShouldReturn401() throws Exception {
        String accessToken = jwtTokenProvider.createAccessToken(usermissing);

        mockMvc.perform(
                        get("/api/v1/users/test")
                                .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.data.errorCode").value(ErrorCode.USER_NOTFOUND.getCode()));
    }
}
