package com.ai_content.job;

import com.ai_content.config.JpaAuditingConfig;
import com.ai_content.job.domain.JobStatus;
import com.ai_content.job.domain.JobType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:db-core;DB_CLOSE_DELAY=-1;INIT=CREATE DOMAIN IF NOT EXISTS JSONB AS JSON"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfig.class)
class JobJpaRepositoryTest {

    private static final List<JobStatus> UNFINISHED = List.of(JobStatus.PENDING, JobStatus.PROCESSING);

    @Autowired
    JobJpaRepository jobJpaRepository;

    @Autowired
    EntityManager entityManager;

    // timestamp(6) keeps microseconds only
    private final LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);

    @Test
    void claim_takesPendingJobOnlyOnce() {
        Long id = job(JobStatus.PENDING, now, null);

        assertThat(claim(id, now.minusMinutes(5))).isEqualTo(1);
        assertThat(claim(id, now.minusMinutes(5))).isZero();

        JobEntity job = jobJpaRepository.findById(id).orElseThrow();
        assertThat(job.getStatus()).isEqualTo(JobStatus.PROCESSING);
        assertThat(job.getStartedAt()).isEqualTo(now);
    }

    @Test
    void claim_reclaimsOnlyStaleProcessingJob() {
        Long fresh = job(JobStatus.PROCESSING, now, now.minusMinutes(1));
        Long stale = job(JobStatus.PROCESSING, now, now.minusMinutes(10));

        assertThat(claim(fresh, now.minusMinutes(5))).isZero();
        assertThat(claim(stale, now.minusMinutes(5))).isEqualTo(1);
    }

    @Test
    void claim_ignoresFinishedJob() {
        Long id = job(JobStatus.COMPLETED, now.minusHours(1), now.minusHours(1));

        assertThat(claim(id, now)).isZero();
    }

    @Test
    void failStuck_failsOnlyOldUnfinishedJobs() {
        Long oldPending = job(JobStatus.PENDING, now.minusHours(1), null);
        Long oldProcessing = job(JobStatus.PROCESSING, now.minusHours(1), now.minusHours(1));
        Long newPending = job(JobStatus.PENDING, now.minusMinutes(1), null);
        Long reclaimedRecently = job(JobStatus.PROCESSING, now.minusHours(1), now.minusMinutes(1));
        Long oldCompleted = job(JobStatus.COMPLETED, now.minusHours(1), now.minusHours(1));

        int failed = jobJpaRepository.failStuck(
                UNFINISHED, JobStatus.FAILED, "timed out", now, now.minusMinutes(30));

        assertThat(failed).isEqualTo(2);
        assertThat(status(oldPending)).isEqualTo(JobStatus.FAILED);
        assertThat(status(oldProcessing)).isEqualTo(JobStatus.FAILED);
        assertThat(status(newPending)).isEqualTo(JobStatus.PENDING);
        assertThat(status(reclaimedRecently)).isEqualTo(JobStatus.PROCESSING);
        assertThat(status(oldCompleted)).isEqualTo(JobStatus.COMPLETED);

        JobEntity job = jobJpaRepository.findById(oldPending).orElseThrow();
        assertThat(job.getErrorMessage()).isEqualTo("timed out");
        assertThat(job.getFinishedAt()).isEqualTo(now);
    }

    private int claim(Long id, LocalDateTime staleBefore) {
        return jobJpaRepository.claim(id, JobStatus.PENDING, JobStatus.PROCESSING, now, staleBefore);
    }

    private JobStatus status(Long id) {
        return jobJpaRepository.findById(id).orElseThrow().getStatus();
    }

    // Auditing always stamps "now", so the timestamps under test are set with a native update
    private Long job(JobStatus status, LocalDateTime createdAt, LocalDateTime startedAt) {
        JobEntity job = jobJpaRepository.saveAndFlush(JobEntity.create(1L, JobType.GENERATE_IDEA, null));
        entityManager.createNativeQuery(
                        "update ai_jobs set status = ?, created_at = ?, updated_at = ?, started_at = ? where id = ?")
                .setParameter(1, status.name())
                .setParameter(2, createdAt)
                .setParameter(3, createdAt)
                .setParameter(4, startedAt)
                .setParameter(5, job.getId())
                .executeUpdate();
        entityManager.clear();
        return job.getId();
    }
}
