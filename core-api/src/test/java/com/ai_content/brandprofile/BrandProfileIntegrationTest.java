package com.ai_content.brandprofile;

import com.ai_content.config.jwt.JwtTokenProvider;
import com.ai_content.service.auth.AuthFacade;
import com.ai_content.user.UserEntity;
import com.ai_content.user.UserJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:55433/brand_profile_test",
        "spring.datasource.username=brand_test",
        "spring.datasource.password=brand_test_password",
        "spring.datasource.driver-class-name=org.postgresql.Driver",
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect"
})
@AutoConfigureMockMvc
@ActiveProfiles("brand-profile-integration")
class BrandProfileIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthFacade authFacade;

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void registrationShouldCreateEmptyProfile() {
        String email = uniqueEmail();

        try {
            authFacade.register(
                    "Integration User",
                    email,
                    "Password123!"
            );

            Long count = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM brand_profiles bp
                    JOIN users u ON u.id = bp.user_id
                    WHERE u.email = ?
                      AND bp.description IS NULL
                      AND bp.tone_of_voice IS NULL
                      AND bp.forbidden_words IS NULL
                      AND bp.brand_colors = '[]'::jsonb
                      AND bp.completeness_pct = 0
                      AND bp.created_at IS NOT NULL
                      AND bp.updated_at IS NOT NULL
                    """, Long.class, email);

            assertEquals(Long.valueOf(1), count);
        } finally {
            deleteTestUser(email);
        }
    }

    @Test
    void getShouldReturn404WithoutCreatingProfile() throws Exception {
        String email = uniqueEmail();

        try {
            // Tạo User trực tiếp, cố ý không tạo Brand Profile.
            UserEntity user = userRepository.saveAndFlush(
                    UserEntity.createUser(
                            "User Without Profile",
                            email,
                            passwordEncoder.encode("Password123!")
                    )
            );

            String token = jwtTokenProvider.createAccessToken(
                    UserEntity.toDomain(user)
            );

            mockMvc.perform(
                            get("/api/v1/brand-profile")
                                    .header(
                                            "Authorization",
                                            "Bearer " + token
                                    )
                    )
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.data.errorCode")
                            .value("BRAND_PROFILE_NOT_FOUND"));

            Long count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM brand_profiles WHERE user_id = ?",
                    Long.class,
                    user.getId()
            );

            assertEquals(Long.valueOf(0), count);
        } finally {
            deleteTestUser(email);
        }
    }

    @Test
    void registrationShouldRollbackWhenProfileInsertFails() {
        String email = uniqueEmail();

        Long profilesBefore = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM brand_profiles",
                Long.class
        );

        // Chỉ trong database test:
        // từ chối hồ sơ mới có điểm 0 để gây lỗi INSERT thực sự.
        jdbcTemplate.execute("""
                ALTER TABLE brand_profiles
                ADD CONSTRAINT test_reject_empty_profile
                CHECK (completeness_pct <> 0) NOT VALID
                """);

        try {
            assertThrows(
                    DataIntegrityViolationException.class,
                    () -> authFacade.register(
                            "Rollback User",
                            email,
                            "Password123!"
                    )
            );

            Long usersAfter = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM users WHERE email = ?",
                    Long.class,
                    email
            );

            Long profilesAfter = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM brand_profiles",
                    Long.class
            );

            assertEquals(Long.valueOf(0), usersAfter);
            assertEquals(profilesBefore, profilesAfter);
        } finally {
            jdbcTemplate.execute("""
                    ALTER TABLE brand_profiles
                    DROP CONSTRAINT IF EXISTS test_reject_empty_profile
                    """);

            deleteTestUser(email);
        }
    }

    private String uniqueEmail() {
        return "brand-it-" + UUID.randomUUID() + "@example.com";
    }

    private void deleteTestUser(String email) {
        jdbcTemplate.update("""
                DELETE FROM brand_profiles
                WHERE user_id IN (
                    SELECT id FROM users WHERE email = ?
                )
                """, email);

        jdbcTemplate.update(
                "DELETE FROM users WHERE email = ?",
                email
        );
    }
}