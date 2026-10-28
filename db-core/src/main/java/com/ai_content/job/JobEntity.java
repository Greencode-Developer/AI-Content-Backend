package com.ai_content.job;

import com.ai_content.BaseEntity;
import com.ai_content.job.domain.JobStatus;
import com.ai_content.job.domain.JobType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Builder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_jobs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "input", columnDefinition = "jsonb")
    private String input;

    @Column(name = "trace_id", length = 64)
    private String traceId;

    @Column(length = 100)
    private String model;

    @Column(name = "prompt_version", length = 50)
    private String promptVersion;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private JobEntity(
            Long userId,
            JobType type,
            JobStatus status,
            String errorMessage,
            String input
    ) {
        this.userId = userId;
        this.type = type;
        this.status = status;
        this.errorMessage = errorMessage;
        this.input = input;
    }

    public static JobEntity create(Long userId, JobType type, String input) {
        return JobEntity.builder()
                .userId(userId)
                .type(type)
                .status(JobStatus.PENDING)
                .input(input)
                .build();
    }

    public void processing() {
        this.status = JobStatus.PROCESSING;
    }

    public void complete() {
        this.status = JobStatus.COMPLETED;
    }

    public void complete(String traceId, String model, String promptVersion) {
        complete();
        this.traceId = traceId;
        this.model = model;
        this.promptVersion = promptVersion;
        this.errorMessage = null;
        this.finishedAt = LocalDateTime.now();
    }

    public void fail(String errorMessage) {
        this.status = JobStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    public void fail(String errorMessage, String traceId) {
        fail(errorMessage);
        this.traceId = traceId;
        this.finishedAt = LocalDateTime.now();
    }
}