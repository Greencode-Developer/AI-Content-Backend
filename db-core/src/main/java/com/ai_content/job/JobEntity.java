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

    @Builder(access = AccessLevel.PRIVATE)
    private JobEntity(
            Long userId,
            JobType type,
            JobStatus status,
            String errorMessage
    ) {
        this.userId = userId;
        this.type = type;
        this.status = status;
        this.errorMessage = errorMessage;
    }

    public static JobEntity create(Long userId, JobType type) {
        return JobEntity.builder()
                .userId(userId)
                .type(type)
                .status(JobStatus.PENDING)
                .build();
    }

    public void processing() {
        this.status = JobStatus.PROCESSING;
    }

    public void complete() {
        this.status = JobStatus.COMPLETED;
    }

    public void fail(String errorMessage) {
        this.status = JobStatus.FAILED;
        this.errorMessage = errorMessage;
    }
}