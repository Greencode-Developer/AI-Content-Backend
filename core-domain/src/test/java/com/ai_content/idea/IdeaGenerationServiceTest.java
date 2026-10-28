package com.ai_content.idea;

import com.ai_content.idea.service.domain.AiIdeaGenerationResult;
import com.ai_content.idea.service.domain.GeneratedIdea;
import com.ai_content.idea.service.domain.IdeaGenerationInput;
import com.ai_content.idea.service.service.AiGenerationException;
import com.ai_content.idea.service.service.AiIdeaGenerator;
import com.ai_content.idea.service.service.IdeaGenerationInputCodec;
import com.ai_content.idea.service.service.IdeaGenerationResultWriter;
import com.ai_content.idea.service.service.IdeaGenerationService;
import com.ai_content.idea.service.service.JobInProgressException;
import com.ai_content.job.domain.AiGenerationMeta;
import com.ai_content.job.domain.Job;
import com.ai_content.job.domain.JobStatus;
import com.ai_content.job.domain.JobType;
import com.ai_content.job.service.AiJobRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdeaGenerationServiceTest {

    @Mock
    private AiJobRepository aiJobRepository;

    @Mock
    private AiIdeaGenerator aiIdeaGenerator;

    @Mock
    private IdeaGenerationResultWriter resultWriter;

    private final IdeaGenerationInputCodec inputCodec = new IdeaGenerationInputCodec(new ObjectMapper());

    private IdeaGenerationService service;

    private static final Long JOB_ID = 100L;
    private static final Long USER_ID = 1L;
    private static final String TRACE_ID = "trace-abc";
    private static final IdeaGenerationInput INPUT =
            new IdeaGenerationInput("Tết sale", "Shop A", "friendly", "", "", null, null, null);
    private static final AiGenerationMeta META = new AiGenerationMeta(TRACE_ID, "claude", "3");
    private static final Duration PROCESSING_TIMEOUT = Duration.ofMinutes(5);

    @BeforeEach
    void setUp() {
        service = new IdeaGenerationService(
                aiJobRepository, aiIdeaGenerator, inputCodec, resultWriter, PROCESSING_TIMEOUT);
    }

    @Test
    void process_shouldSkip_whenJobAlreadyFinished() {
        when(aiJobRepository.markProcessing(eq(JOB_ID), any())).thenReturn(false);
        when(aiJobRepository.findById(JOB_ID)).thenReturn(Optional.of(job(JobStatus.COMPLETED, null)));

        service.process(JOB_ID);

        verifyNoInteractions(aiIdeaGenerator, resultWriter);
        verify(aiJobRepository, never()).markFailed(any(), any(), any());
    }

    @Test
    void process_shouldSkip_whenJobIsMissing() {
        when(aiJobRepository.markProcessing(eq(JOB_ID), any())).thenReturn(false);
        when(aiJobRepository.findById(JOB_ID)).thenReturn(Optional.empty());

        service.process(JOB_ID);

        verifyNoInteractions(aiIdeaGenerator, resultWriter);
    }

    @Test
    void process_shouldThrowForRetry_whenJobStillProcessingElsewhere() {
        when(aiJobRepository.markProcessing(eq(JOB_ID), any())).thenReturn(false);
        when(aiJobRepository.findById(JOB_ID)).thenReturn(Optional.of(job(JobStatus.PROCESSING, null)));

        assertThatThrownBy(() -> service.process(JOB_ID)).isInstanceOf(JobInProgressException.class);

        verifyNoInteractions(aiIdeaGenerator, resultWriter);
        verify(aiJobRepository, never()).markFailed(any(), any(), any());
    }

    @Test
    void process_shouldClaimWithStaleCutoffFromProcessingTimeout() {
        claimedJob(inputCodec.toJson(INPUT));
        when(aiIdeaGenerator.generate(JOB_ID, USER_ID, INPUT)).thenReturn(result(List.of(idea("Idea 1"))));
        LocalDateTime before = LocalDateTime.now();

        service.process(JOB_ID);

        ArgumentCaptor<LocalDateTime> staleBefore = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(aiJobRepository).markProcessing(eq(JOB_ID), staleBefore.capture());
        assertThat(staleBefore.getValue())
                .isBetween(before.minus(PROCESSING_TIMEOUT), LocalDateTime.now().minus(PROCESSING_TIMEOUT));
    }

    @Test
    void process_shouldGenerateAndSave() {
        Job job = claimedJob(inputCodec.toJson(INPUT));
        AiIdeaGenerationResult result = result(List.of(idea("Idea 1"), idea("Idea 2")));
        when(aiIdeaGenerator.generate(JOB_ID, USER_ID, INPUT)).thenReturn(result);

        service.process(JOB_ID);

        verify(resultWriter).save(job, INPUT, result);
        verify(aiJobRepository, never()).markFailed(any(), any(), any());
    }

    @Test
    void process_shouldFailJob_whenAiServiceFails() {
        claimedJob(inputCodec.toJson(INPUT));
        when(aiIdeaGenerator.generate(JOB_ID, USER_ID, INPUT))
                .thenThrow(new AiGenerationException("AI service returned 500: boom", TRACE_ID));

        service.process(JOB_ID);

        verify(aiJobRepository).markFailed(JOB_ID, "AI service returned 500: boom", TRACE_ID);
        verifyNoInteractions(resultWriter);
    }

    @Test
    void process_shouldFailJob_whenAiReturnsNoIdeas() {
        claimedJob(inputCodec.toJson(INPUT));
        when(aiIdeaGenerator.generate(JOB_ID, USER_ID, INPUT)).thenReturn(result(List.of()));

        service.process(JOB_ID);

        verify(aiJobRepository).markFailed(JOB_ID, "AI returned no ideas", TRACE_ID);
        verifyNoInteractions(resultWriter);
    }

    @Test
    void process_shouldFailJob_whenIdeaHasNoTitle() {
        claimedJob(inputCodec.toJson(INPUT));
        when(aiIdeaGenerator.generate(JOB_ID, USER_ID, INPUT))
                .thenReturn(result(List.of(idea("Idea 1"), idea("  "))));

        service.process(JOB_ID);

        verify(aiJobRepository).markFailed(JOB_ID, "AI returned an idea without title", TRACE_ID);
        verifyNoInteractions(resultWriter);
    }

    @Test
    void process_shouldFailJob_withTraceId_whenAiReturnsNullIdea() {
        claimedJob(inputCodec.toJson(INPUT));
        when(aiIdeaGenerator.generate(JOB_ID, USER_ID, INPUT))
                .thenReturn(result(Arrays.asList(idea("Idea 1"), null)));

        service.process(JOB_ID);

        verify(aiJobRepository).markFailed(JOB_ID, "AI returned an idea without title", TRACE_ID);
        verifyNoInteractions(resultWriter);
    }

    @Test
    void process_shouldFailJob_whenTitleTooLong() {
        claimedJob(inputCodec.toJson(INPUT));
        when(aiIdeaGenerator.generate(JOB_ID, USER_ID, INPUT))
                .thenReturn(result(List.of(idea("x".repeat(256)))));

        service.process(JOB_ID);

        verify(aiJobRepository).markFailed(eq(JOB_ID), startsWith("AI returned a title longer than"), eq(TRACE_ID));
        verifyNoInteractions(resultWriter);
    }

    @Test
    void process_shouldFailJob_whenSavingFails() {
        Job job = claimedJob(inputCodec.toJson(INPUT));
        AiIdeaGenerationResult result = result(List.of(idea("Idea 1")));
        when(aiIdeaGenerator.generate(JOB_ID, USER_ID, INPUT)).thenReturn(result);
        doThrow(new RuntimeException("db down")).when(resultWriter).save(job, INPUT, result);

        service.process(JOB_ID);

        verify(aiJobRepository).markFailed(JOB_ID, "Unexpected error: db down", TRACE_ID);
    }

    @Test
    void process_shouldFailJob_withoutCallingAi_whenInputIsMissing() {
        claimedJob(null);

        service.process(JOB_ID);

        verify(aiJobRepository).markFailed(eq(JOB_ID), anyString(), isNull());
        verifyNoInteractions(aiIdeaGenerator, resultWriter);
    }

    private Job claimedJob(String input) {
        Job job = job(JobStatus.PROCESSING, input);
        when(aiJobRepository.markProcessing(eq(JOB_ID), any())).thenReturn(true);
        when(aiJobRepository.findById(JOB_ID)).thenReturn(Optional.of(job));
        return job;
    }

    private Job job(JobStatus status, String input) {
        return Job.of(JOB_ID, USER_ID, JobType.GENERATE_IDEA, status, null, input);
    }

    private AiIdeaGenerationResult result(List<GeneratedIdea> ideas) {
        return new AiIdeaGenerationResult(ideas, META);
    }

    private GeneratedIdea idea(String title) {
        return new GeneratedIdea(title, "angle", "hook", "reason");
    }
}
