package com.ai_content.job;

import com.ai_content.job.service.AiJobRepository;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FailStuckAiJobsJobTest {

    private static final Duration STUCK_AFTER = Duration.ofMinutes(30);

    @Mock
    AiJobRepository aiJobRepository;

    @Test
    void run_isGuardedBySchedulerLock() throws Exception {
        SchedulerLock lock = FailStuckAiJobsJob.class
                .getMethod("run")
                .getAnnotation(SchedulerLock.class);

        assertThat(lock).isNotNull();
        assertThat(lock.name()).isEqualTo("failStuckAiJobs");
    }

    @Test
    void run_failsJobsOlderThanStuckAfter() {
        LocalDateTime before = LocalDateTime.now();

        new FailStuckAiJobsJob(aiJobRepository, STUCK_AFTER).run();

        ArgumentCaptor<LocalDateTime> cutoff = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(aiJobRepository).failStuckJobs(cutoff.capture(), eq(FailStuckAiJobsJob.TIMEOUT_MESSAGE));
        assertThat(cutoff.getValue()).isCloseTo(before.minus(STUCK_AFTER), within(5, ChronoUnit.SECONDS));
    }

    @Test
    void run_whenRepositoryFails_doesNotThrow() {
        when(aiJobRepository.failStuckJobs(any(), anyString())).thenThrow(new RuntimeException("DB down"));

        assertThatCode(() -> new FailStuckAiJobsJob(aiJobRepository, STUCK_AFTER).run())
                .doesNotThrowAnyException();
    }
}
