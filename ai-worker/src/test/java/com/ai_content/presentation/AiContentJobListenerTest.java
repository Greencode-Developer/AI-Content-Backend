package com.ai_content.presentation;

import com.ai_content.application.ProcessAiJobUseCase;
import com.ai_content.idea.service.service.JobInProgressException;
import com.ai_content.job.domain.JobType;
import com.ai_content.message.AiJobMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AiContentJobListenerTest {

    @Mock
    ProcessAiJobUseCase processAiJobUseCase;

    @InjectMocks
    AiContentJobListener listener;

    private static final AiJobMessage MESSAGE = new AiJobMessage(10L, 1L, JobType.GENERATE_IDEA);

    @Test
    void process_whenSuccess_delegatesToUseCase() {
        listener.process(MESSAGE);

        verify(processAiJobUseCase).execute(10L, JobType.GENERATE_IDEA);
    }

    @Test
    void process_whenInvalidMessage_dropsMessage() {
        AiJobMessage invalid = new AiJobMessage(null, 1L, null);
        doThrow(new IllegalArgumentException("jobId and type are required"))
                .when(processAiJobUseCase).execute(null, null);

        assertThatCode(() -> listener.process(invalid)).doesNotThrowAnyException();
    }

    @Test
    void process_whenInfrastructureError_rethrowsForSqsRetry() {
        doThrow(new DataAccessResourceFailureException("db down"))
                .when(processAiJobUseCase).execute(10L, JobType.GENERATE_IDEA);

        assertThatThrownBy(() -> listener.process(MESSAGE))
                .isInstanceOf(DataAccessResourceFailureException.class);
    }

    @Test
    void process_whenJobStillInProgress_rethrowsForSqsRetry() {
        doThrow(new JobInProgressException(10L))
                .when(processAiJobUseCase).execute(10L, JobType.GENERATE_IDEA);

        assertThatThrownBy(() -> listener.process(MESSAGE))
                .isInstanceOf(JobInProgressException.class);
    }
}
