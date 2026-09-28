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
import java.util.List;
import com.ai_content.brandprofile.domain.BrandProfile;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.hamcrest.Matchers.is;

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
                        "Registration succeeded, but profile was not found"
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

    @Test
    void putShouldNormalizeColorsAndPersistCompleteness() throws Exception {
        String email = uniqueEmail();
        try {
            String token = registerAndGetToken(email);
            BrandProfile before = storedProfile(email);
            putProfile(token, """
                    {
                      "description": "  Coffee brand  ",
                      "tone_of_voice": "  Warm  ",
                      "forbidden_words": "  cheapest  ",
                      "brand_colors": [" #aabbcc ", "#AABBCC", "#FFFFFF"]
                    }
                    """)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(is(before.id()), Long.class))
                    .andExpect(jsonPath("$.description").value("Coffee brand"))
                    .andExpect(jsonPath("$.tone_of_voice").value("Warm"))
                    .andExpect(jsonPath("$.forbidden_words").value("cheapest"))
                    .andExpect(jsonPath("$.brand_colors.length()").value(2))
                    .andExpect(jsonPath("$.brand_colors[0]").value("#AABBCC"))
                    .andExpect(jsonPath("$.brand_colors[1]").value("#FFFFFF"))
                    .andExpect(jsonPath("$.completeness_pct").value(100));

            getProfile(token)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.description").value("Coffee brand"))
                    .andExpect(jsonPath("$.completeness_pct").value(100));

            BrandProfile after = storedProfile(email);
            assertEquals(before.id(), after.id());
            assertEquals(before.userId(), after.userId());
            assertEquals(before.createdAt(), after.createdAt());
            assertEquals(List.of("#AABBCC", "#FFFFFF"), after.brandColors());
            assertEquals(100, after.completenessPct());
        } finally {
            deleteTestUser(email);
        }
    }

    @Test
    void putShouldRecalculateCompletenessWhenFieldsAreCleared() throws Exception {
        String email = uniqueEmail();
        try {
            String token = registerAndGetToken(email);
            putProfile(token, """
                    {"description":"Coffee brand", "tone_of_voice":"Warm",
                     "forbidden_words":null, "brand_colors":["#AABBCC"]}
                    """)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.completeness_pct").value(100));

            putProfile(token, """
                    {"description":"Coffee brand", "tone_of_voice":null,
                     "forbidden_words":null, "brand_colors":["#AABBCC"]}
                    """)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.completeness_pct").value(60));
            assertEquals(60, storedProfile(email).completenessPct());

            putProfile(token, """
                    {"description":null, "tone_of_voice":null,
                     "forbidden_words":null, "brand_colors":[]}
                    """)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.completeness_pct").value(0));

            getProfile(token)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.description").isEmpty())
                    .andExpect(jsonPath("$.tone_of_voice").isEmpty())
                    .andExpect(jsonPath("$.forbidden_words").isEmpty())
                    .andExpect(jsonPath("$.brand_colors").isEmpty())
                    .andExpect(jsonPath("$.completeness_pct").value(0));
            BrandProfile cleared = storedProfile(email);
            assertNull(cleared.description());
            assertNull(cleared.toneOfVoice());
            assertNull(cleared.forbiddenWords());
            assertTrue(cleared.brandColors().isEmpty());
            assertEquals(0, cleared.completenessPct());
        } finally {
            deleteTestUser(email);
        }
    }

    @Test
    void invalidPutShouldNotChangeStoredProfile() throws Exception {
        String email = uniqueEmail();
        try {
            String token = registerAndGetToken(email);
            putProfile(token, """
                    {"description":"Original brand", "tone_of_voice":"Warm",
                     "forbidden_words":null, "brand_colors":["#112233"]}
                    """).andExpect(status().isOk());
            BrandProfile before = storedProfile(email);

            putProfile(token, """
                    {"description":"Must not be saved", "tone_of_voice":null,
                     "forbidden_words":null, "brand_colors":["red"]}
                    """)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.errorCode").value("VALIDATION_ERROR"));

            assertEquals(before, storedProfile(email));
        } finally {
            deleteTestUser(email);
        }
    }

    @Test
    void usersShouldOnlyAccessTheirOwnProfiles() throws Exception {
        String emailA = uniqueEmail();
        String emailB = uniqueEmail();
        try {
            String tokenA = registerAndGetToken(emailA);
            String tokenB = registerAndGetToken(emailB);
            BrandProfile beforeA = storedProfile(emailA);
            BrandProfile beforeB = storedProfile(emailB);
            assertNotEquals(beforeA.id(), beforeB.id());

            mockMvc.perform(get("/api/v1/brand-profile")
                            .param("user_id", beforeB.userId().toString())
                            .header("Authorization", "Bearer " + tokenA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(is(beforeA.id()), Long.class));

            // Neither the owner nor the profile ID may be chosen by the client.
            for (String forbiddenField : List.of("user_id", "id")) {
                Long target = forbiddenField.equals("user_id")
                        ? beforeB.userId() : beforeB.id();
                putProfile(tokenA, """
                        {"description":"Attempt to change B", "tone_of_voice":null,
                         "forbidden_words":null, "brand_colors":[], "%s":%d}
                        """.formatted(forbiddenField, target))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.data.errorCode").value("VALIDATION_ERROR"));
                assertEquals(beforeA, storedProfile(emailA));
                assertEquals(beforeB, storedProfile(emailB));
            }

            mockMvc.perform(put("/api/v1/brand-profile")
                            .param("user_id", beforeB.userId().toString())
                            .header("Authorization", "Bearer " + tokenA)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"description":"Brand A updated", "tone_of_voice":null,
                                     "forbidden_words":null, "brand_colors":[]}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(is(beforeA.id()), Long.class));

            assertEquals("Brand A updated", storedProfile(emailA).description());
            assertEquals(beforeB, storedProfile(emailB));
            getProfile(tokenB)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(is(beforeB.id()), Long.class))
                    .andExpect(jsonPath("$.description").isEmpty())
                    .andExpect(jsonPath("$.completeness_pct").value(0));
        } finally {
            deleteTestUser(emailA);
            deleteTestUser(emailB);
        }
    }

    private String registerAndGetToken(String email) {
        return authFacade.register("Brand Profile Tester", email, "Password123!")
                .accessToken();
    }

    private BrandProfile storedProfile(String email) {
        Long userId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE email = ?", Long.class, email);
        return brandProfileRepository.findByUserId(userId).orElseThrow().toDomain();
    }

    private ResultActions putProfile(String token, String body) throws Exception {
        return mockMvc.perform(put("/api/v1/brand-profile")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private ResultActions getProfile(String token) throws Exception {
        return mockMvc.perform(get("/api/v1/brand-profile")
                .header("Authorization", "Bearer " + token));
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