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
        "spring.datasource.url=jdbc:h2:mem:brand_profile_integration;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.jpa.properties.hibernate.hbm2ddl.halt_on_error=true"
})

@AutoConfigureMockMvc

@ActiveProfiles("test")
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

    @Autowired
    private BrandProfileJpaRepository brandProfileRepository;

    @Test
    void registrationShouldCreateEmptyProfile() {
        String email = uniqueEmail();

        try {
            authFacade.register(
                    "Integration User",
                    email,
                    "Password123!"
            );

            Long userId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE email = ?",
                Long.class,
                email
            );

            BrandProfileEntity profile = brandProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AssertionError(
                        "Registration successfull, but profile not found"
                ));

            assertNotNull(profile.getId());
            assertEquals(userId, profile.getUserId());
            assertNull(profile.getDescription());
            assertNull(profile.getToneOfVoice());
            assertNull(profile.getForbiddenWords());
            assertNotNull(profile.getBrandColors());
            assertTrue(profile.getBrandColors().isEmpty());
            assertEquals(0, profile.getCompletenessPct());
            assertNotNull(profile.getCreatedAt());
            assertNotNull(profile.getUpdatedAt());
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
                CHECK (completeness_pct <> 0)
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