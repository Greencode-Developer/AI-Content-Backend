package com.ai_content.job;

import com.ai_content.job.service.AiJobRepository;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/*
 * Marks AI jobs that will never finish as FAILED
 *   - message moved to the DLQ after all retries
 *   - message never published
 * Without this the job stays PENDING/PROCESSING forever.
 */
@Component
public class FailStuckAiJobsJob {

    private static final Logger log = LoggerFactory.getLogger(FailStuckAiJobsJob.class);

    static final String TIMEOUT_MESSAGE = "AI job timed out, please try again";

    private final AiJobRepository aiJobRepository;
    private final Duration stuckAfter;

    public FailStuckAiJobsJob(
            AiJobRepository aiJobRepository,
            @Value("${schedule.ai-job-timeout.stuck-after:PT30M}") Duration stuckAfter
    ) {
        this.aiJobRepository = aiJobRepository;
        this.stuckAfter = stuckAfter;
    }

    @Scheduled(cron = "${schedule.ai-job-timeout.cron}", zone = "Asia/Ho_Chi_Minh")
    @SchedulerLock(
            name = "failStuckAiJobs",
            lockAtLeastFor = "${schedule.ai-job-timeout.lock-at-least-for}",
            lockAtMostFor = "${schedule.ai-job-timeout.lock-at-most-for}"
    )
    public void run() {
        try {
            int failed = aiJobRepository.failStuckJobs(LocalDateTime.now().minus(stuckAfter), TIMEOUT_MESSAGE);
            if (failed > 0) {
                log.warn("Marked {} stuck AI jobs as FAILED (older than {})", failed, stuckAfter);
            }
        } catch (Exception e) {
            log.error("FailStuckAiJobsJob failed: {}", e.getMessage(), e);
        }
    }
}
