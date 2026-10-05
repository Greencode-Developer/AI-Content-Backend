package com.ai_content.idea;

import com.ai_content.idea.service.IdeaService;
import com.ai_content.job.domain.Job;
import com.ai_content.job.domain.JobType;
import com.ai_content.job.service.AiJobRepository;
import com.ai_content.job.sqs.AiJobPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdeaServiceTest {

    @Mock
    private AiJobRepository aiJobRepository;

    @Mock
    private AiJobPublisher aiJobPublisher;

    @InjectMocks
    private IdeaService service;

    @Test
    void generateIdea_shouldCreateJobAndReturnJobId() {
        Long userId = 1L;
        Long jobId = 100L;

        Job job = mock(Job.class);
        when(job.id()).thenReturn(jobId);

        when(aiJobRepository.createJob(userId, JobType.GENERATE_IDEA))
                .thenReturn(job);

        Long result = service.generateIdea(userId);

        assertThat(result).isEqualTo(jobId);

        verify(aiJobRepository)
                .createJob(userId, JobType.GENERATE_IDEA);
    }
    @Test
    void generateIdea_shouldThrow_whenCreateJobFails() {
        Long userId = 1L;

        when(aiJobRepository.createJob(userId, JobType.GENERATE_IDEA))
                .thenThrow(new RuntimeException("Database error"));

        assertThatThrownBy(() -> service.generateIdea(userId))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void generateIdea_shouldPublishJobCommand() {
        Long userId = 1L;
        Long jobId = 100L;

        Job job = mock(Job.class);
        when(job.id()).thenReturn(jobId);

        when(aiJobRepository.createJob(userId, JobType.GENERATE_IDEA))
                .thenReturn(job);

        service.generateIdea(userId);

        verify(aiJobPublisher).publish(
                argThat(command ->
                        command.jobId().equals(jobId)
                                && command.userId().equals(userId)
                )
        );
    }
}