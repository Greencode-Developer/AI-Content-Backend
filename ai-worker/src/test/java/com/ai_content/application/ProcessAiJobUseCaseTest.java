package com.ai_content.application;

import com.ai_content.idea.service.service.IdeaGenerationService;
import com.ai_content.job.domain.JobType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ProcessAiJobUseCaseTest {

    @Mock
    IdeaGenerationService ideaGenerationService;

    @InjectMocks
    ProcessAiJobUseCase useCase;

    @Test
    void execute_generateIdea_delegatesToIdeaGenerationService() {
        useCase.execute(10L, JobType.GENERATE_IDEA);

        verify(ideaGenerationService).process(10L);
    }

    @Test
    void execute_withoutJobId_throwsIllegalArgument() {
        assertThatThrownBy(() -> useCase.execute(null, JobType.GENERATE_IDEA))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(ideaGenerationService);
    }

    @Test
    void execute_withoutType_throwsIllegalArgument() {
        assertThatThrownBy(() -> useCase.execute(10L, null))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(ideaGenerationService);
    }
}
