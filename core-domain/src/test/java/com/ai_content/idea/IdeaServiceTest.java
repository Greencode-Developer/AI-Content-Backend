package com.ai_content.idea;

import com.ai_content.common.error.CustomException;
import com.ai_content.common.error.ErrorCode;
import com.ai_content.idea.service.command.GenerateIdeaCommand;
import com.ai_content.idea.service.domain.Idea;
import com.ai_content.idea.service.domain.IdeaGenerationInput;
import com.ai_content.idea.service.domain.JobIdeas;
import com.ai_content.idea.service.service.IdeaGenerationInputAssembler;
import com.ai_content.idea.service.service.IdeaGenerationInputCodec;
import com.ai_content.idea.service.service.IdeaRepository;
import com.ai_content.idea.service.service.IdeaService;
import com.ai_content.job.domain.Job;
import com.ai_content.job.domain.JobStatus;
import com.ai_content.job.domain.JobType;
import com.ai_content.job.service.AiJobRepository;
import com.ai_content.job.sqs.AiJobPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdeaServiceTest {

    @Mock
    private AiJobRepository aiJobRepository;

    @Mock
    private AiJobPublisher aiJobPublisher;

    @Mock
    private IdeaRepository ideaRepository;

    @Mock
    private IdeaGenerationInputAssembler inputAssembler;

    private final IdeaGenerationInputCodec inputCodec = new IdeaGenerationInputCodec(new ObjectMapper());

    private IdeaService service;

    private static final Long USER_ID = 1L;
    private static final Long JOB_ID = 100L;
    private static final GenerateIdeaCommand COMMAND = new GenerateIdeaCommand("Tết sale", 2L, 3L, null);
    private static final IdeaGenerationInput INPUT =
            new IdeaGenerationInput("Tết sale", "Shop A", "friendly", "Mẹ bỉm", "Khuyến mãi", 2L, 3L, null);

    @BeforeEach
    void setUp() {
        service = new IdeaService(aiJobRepository, aiJobPublisher, ideaRepository, inputAssembler, inputCodec);
    }

    @Test
    void generateIdea_shouldStoreInputSnapshotAndReturnJobId() {
        when(inputAssembler.assemble(USER_ID, COMMAND)).thenReturn(INPUT);
        when(aiJobRepository.createJob(eq(USER_ID), eq(JobType.GENERATE_IDEA), anyString()))
                .thenReturn(job(USER_ID, JobStatus.PENDING));

        Long result = service.generateIdea(USER_ID, COMMAND);

        assertThat(result).isEqualTo(JOB_ID);
        ArgumentCaptor<String> inputJson = ArgumentCaptor.forClass(String.class);
        verify(aiJobRepository).createJob(eq(USER_ID), eq(JobType.GENERATE_IDEA), inputJson.capture());
        assertThat(inputCodec.fromJson(inputJson.getValue())).isEqualTo(INPUT);
    }

    @Test
    void generateIdea_shouldPublishJobCommand() {
        when(inputAssembler.assemble(USER_ID, COMMAND)).thenReturn(INPUT);
        when(aiJobRepository.createJob(eq(USER_ID), eq(JobType.GENERATE_IDEA), anyString()))
                .thenReturn(job(USER_ID, JobStatus.PENDING));

        service.generateIdea(USER_ID, COMMAND);

        verify(aiJobPublisher).publish(argThat(command ->
                command.jobId().equals(JOB_ID)
                        && command.userId().equals(USER_ID)
                        && command.type() == JobType.GENERATE_IDEA));
    }

    @Test
    void generateIdea_shouldNotCreateJob_whenReferenceIsInvalid() {
        when(inputAssembler.assemble(USER_ID, COMMAND))
                .thenThrow(new CustomException(ErrorCode.PERSONA_NOT_FOUND));

        assertThatThrownBy(() -> service.generateIdea(USER_ID, COMMAND))
                .isInstanceOf(CustomException.class);

        verify(aiJobRepository, never()).createJob(any(), any(), any());
        verify(aiJobPublisher, never()).publish(any());
    }

    @Test
    void generateIdea_shouldThrow_whenCreateJobFails() {
        when(inputAssembler.assemble(USER_ID, COMMAND)).thenReturn(INPUT);
        when(aiJobRepository.createJob(eq(USER_ID), eq(JobType.GENERATE_IDEA), anyString()))
                .thenThrow(new RuntimeException("Database error"));

        assertThatThrownBy(() -> service.generateIdea(USER_ID, COMMAND))
                .isInstanceOf(RuntimeException.class);

        verify(aiJobPublisher, never()).publish(any());
    }

    @Test
    void getJobIdeas_shouldReturnJobWithIdeas() {
        Job job = job(USER_ID, JobStatus.COMPLETED);
        List<Idea> ideas = List.of(mock(Idea.class));
        when(aiJobRepository.findById(JOB_ID)).thenReturn(Optional.of(job));
        when(ideaRepository.findAllByJobId(JOB_ID)).thenReturn(ideas);

        JobIdeas result = service.getJobIdeas(USER_ID, JOB_ID);

        assertThat(result.job()).isEqualTo(job);
        assertThat(result.ideas()).isEqualTo(ideas);
    }

    @Test
    void getJobIdeas_shouldThrowNotFound_whenJobMissing() {
        when(aiJobRepository.findById(JOB_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getJobIdeas(USER_ID, JOB_ID))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AI_JOB_NOT_FOUND);
    }

    @Test
    void getJobIdeas_shouldThrowAccessDenied_whenJobBelongsToOtherUser() {
        when(aiJobRepository.findById(JOB_ID)).thenReturn(Optional.of(job(99L, JobStatus.COMPLETED)));

        assertThatThrownBy(() -> service.getJobIdeas(USER_ID, JOB_ID))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AI_JOB_ACCESS_DENIED);

        verify(ideaRepository, never()).findAllByJobId(any());
    }

    private Job job(Long ownerId, JobStatus status) {
        return Job.of(JOB_ID, ownerId, JobType.GENERATE_IDEA, status, null, null);
    }
}
