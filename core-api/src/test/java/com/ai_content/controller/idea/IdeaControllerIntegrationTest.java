package com.ai_content.controller.idea;

import com.ai_content.common.error.ErrorCode;
import com.ai_content.config.jwt.JwtTokenProvider;
import com.ai_content.idea.IdeaEntity;
import com.ai_content.idea.IdeaJpaRepository;
import com.ai_content.idea.service.domain.IdeaSource;
import com.ai_content.idea.service.domain.Platform;
import com.ai_content.job.JobEntity;
import com.ai_content.job.JobJpaRepository;
import com.ai_content.job.domain.JobStatus;
import com.ai_content.job.domain.JobType;
import com.ai_content.job.sqs.AiJobPublisher;
import com.ai_content.persona.PersonaEntity;
import com.ai_content.persona.PersonaJpaRepository;
import com.ai_content.pillar.PillarEntity;
import com.ai_content.pillar.PillarJpaRepository;
import com.ai_content.user.UserEntity;
import com.ai_content.user.UserJpaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
// ai_jobs.input is jsonb: same H2 workaround as TrendSignalControllerIntegrationTest
@TestPropertySource(properties =
        "spring.datasource.url=jdbc:h2:mem:idea;DB_CLOSE_DELAY=-1;INIT=CREATE DOMAIN IF NOT EXISTS JSONB AS JSON")
@Transactional
class IdeaControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JwtTokenProvider jwtTokenProvider;
    @Autowired UserJpaRepository userJpaRepository;
    @Autowired PersonaJpaRepository personaJpaRepository;
    @Autowired PillarJpaRepository pillarJpaRepository;
    @Autowired JobJpaRepository jobJpaRepository;
    @Autowired IdeaJpaRepository ideaJpaRepository;

    @MockBean AiJobPublisher aiJobPublisher;

    private UserEntity user;
    private UserEntity otherUser;
    private String token;
    private String otherUserToken;

    @BeforeEach
    void setUp() {
        user = saveUser();
        otherUser = saveUser();
        token = jwtTokenProvider.createAccessToken(UserEntity.toDomain(user));
        otherUserToken = jwtTokenProvider.createAccessToken(UserEntity.toDomain(otherUser));
    }

    // ─── POST /generate ──────────────────────────────────────────────────────

    @Test
    void generate_withoutBody_createsPendingJob() throws Exception {
        String response = generate(null, token)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long jobId = objectMapper.readTree(response).get("jobId").asLong();
        JobEntity job = jobJpaRepository.findById(jobId).orElseThrow();
        assertThat(job.getStatus()).isEqualTo(JobStatus.PENDING);
        assertThat(job.getUserId()).isEqualTo(user.getId());
        assertThat(input(job).get("brandName").asText()).isEqualTo("user");
    }

    @Test
    void generate_publishesOnlyAfterCommit() throws Exception {
        String response = generate(null, token)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Long jobId = objectMapper.readTree(response).get("jobId").asLong();

        // still inside the test transaction: nothing may be sent yet
        verify(aiJobPublisher, never()).publish(any());

        TestTransaction.flagForCommit();
        TestTransaction.end();

        verify(aiJobPublisher).publish(argThat(command -> command.jobId().equals(jobId)));
    }

    @Test
    void generate_withPersonaAndPillar_storesInputSnapshot() throws Exception {
        PersonaEntity persona = personaJpaRepository.save(
                PersonaEntity.create(user.getId(), "Mẹ bỉm", "25-35", "Nội trợ", "Ít thời gian", "Tiết kiệm", "Rẻ không?"));
        PillarEntity pillar = pillarJpaRepository.save(
                PillarEntity.create(user.getId(), "Khuyến mãi", "Tăng doanh số", false));

        String body = """
                {"topic":"Tết sale","personaId":%d,"pillarId":%d}
                """.formatted(persona.getId(), pillar.getId());
        String response = generate(body, token)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode input = input(jobJpaRepository.findById(
                objectMapper.readTree(response).get("jobId").asLong()).orElseThrow());
        assertThat(input.get("topic").asText()).isEqualTo("Tết sale");
        assertThat(input.get("persona").asText()).isEqualTo("Mẹ bỉm. Age: 25-35. Occupation: Nội trợ. Pain points: Ít thời gian. "
                + "Desires: Tiết kiệm. Typical phrases: Rẻ không?");
        assertThat(input.get("contentPillar").asText()).isEqualTo("Khuyến mãi. Purpose: Tăng doanh số");
        assertThat(input.get("personaId").asLong()).isEqualTo(persona.getId());
        assertThat(input.get("pillarId").asLong()).isEqualTo(pillar.getId());
    }

    @Test
    void generate_withOtherUsersPersona_returns404AndCreatesNoJob() throws Exception {
        PersonaEntity persona = personaJpaRepository.save(
                PersonaEntity.create(otherUser.getId(), "Other", "18-24", "Student", "x", "y", "z"));
        long jobsBefore = jobJpaRepository.count();

        generate("{\"personaId\":%d}".formatted(persona.getId()), token)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data.errorCode").value(ErrorCode.PERSONA_NOT_FOUND.getCode()));

        assertThat(jobJpaRepository.count()).isEqualTo(jobsBefore);
        verify(aiJobPublisher, never()).publish(any());
    }

    @Test
    void generate_withOtherUsersPillar_returns404() throws Exception {
        PillarEntity pillar = pillarJpaRepository.save(
                PillarEntity.create(otherUser.getId(), "Other pillar", "Other purpose", false));

        generate("{\"pillarId\":%d}".formatted(pillar.getId()), token)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data.errorCode").value(ErrorCode.PILLAR_NOTFOUND.getCode()));
    }

    @Test
    void generate_withUnknownTrendSignal_returns404() throws Exception {
        generate("{\"trendSignalId\":999999}", token)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data.errorCode").value(ErrorCode.TREND_SIGNAL_NOT_FOUND.getCode()));
    }

    @Test
    void generate_withTooLongTopic_returns400() throws Exception {
        generate("{\"topic\":\"%s\"}".formatted("x".repeat(501)), token)
                .andExpect(status().isBadRequest());

        verify(aiJobPublisher, never()).publish(any());
    }

    @Test
    void generate_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/ideas/generate")).andExpect(status().isUnauthorized());
    }

    // ─── GET /jobs/{jobId} ───────────────────────────────────────────────────

    @Test
    void getJob_pending_returnsStatusWithoutIdeas() throws Exception {
        JobEntity job = jobJpaRepository.save(JobEntity.create(user.getId(), JobType.GENERATE_IDEA, null));

        getJob(job.getId(), token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value(job.getId()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.errorMessage").value(nullValue()))
                .andExpect(jsonPath("$.ideas", hasSize(0)));
    }

    @Test
    void getJob_completed_returnsIdeas() throws Exception {
        JobEntity job = JobEntity.create(user.getId(), JobType.GENERATE_IDEA, null);
        job.complete("trace-abc", "claude", "3");
        jobJpaRepository.save(job);
        ideaJpaRepository.save(IdeaEntity.create(user.getId(), Platform.FANPAGE, "Idea 1", "angle", "hook", "why",
                null, job.getId(), null, null, IdeaSource.AI_SUGGESTED, false));

        getJob(job.getId(), token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.ideas[*].title", contains("Idea 1")))
                .andExpect(jsonPath("$.ideas[0].approachAngle").value("angle"))
                .andExpect(jsonPath("$.ideas[0].hookSentence").value("hook"))
                .andExpect(jsonPath("$.ideas[0].reason").value("why"));
    }

    @Test
    void getJob_failed_returnsErrorMessage() throws Exception {
        JobEntity job = JobEntity.create(user.getId(), JobType.GENERATE_IDEA, null);
        job.fail("AI service returned 500: boom", "trace-abc");
        jobJpaRepository.save(job);

        getJob(job.getId(), token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.errorMessage").value("AI service returned 500: boom"))
                .andExpect(jsonPath("$.ideas", hasSize(0)));
    }

    @Test
    void getJob_ofOtherUser_returns403() throws Exception {
        JobEntity job = jobJpaRepository.save(JobEntity.create(user.getId(), JobType.GENERATE_IDEA, null));

        getJob(job.getId(), otherUserToken)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.data.errorCode").value(ErrorCode.AI_JOB_ACCESS_DENIED.getCode()));
    }

    @Test
    void getJob_unknown_returns404() throws Exception {
        getJob(999_999L, token)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data.errorCode").value(ErrorCode.AI_JOB_NOT_FOUND.getCode()));
    }

    private ResultActions generate(String body, String accessToken) throws Exception {
        var request = post("/api/v1/ideas/generate").header("Authorization", "Bearer " + accessToken);
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(body);
        }
        return mockMvc.perform(request);
    }

    private ResultActions getJob(Long jobId, String accessToken) throws Exception {
        return mockMvc.perform(get("/api/v1/ideas/jobs/" + jobId).header("Authorization", "Bearer " + accessToken));
    }

    private JsonNode input(JobEntity job) throws Exception {
        return objectMapper.readTree(job.getInput());
    }

    private UserEntity saveUser() {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        return userJpaRepository.save(UserEntity.createUser("user", unique + "@test.com", "password"));
    }
}
