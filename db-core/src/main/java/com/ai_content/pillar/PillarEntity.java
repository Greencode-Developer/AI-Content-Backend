package com.ai_content.pillar;

import com.ai_content.BaseEntity;
import com.ai_content.pillar.domain.Pillar;
import com.ai_content.pillar.domain.PillarStatus;
import com.ai_content.user.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "content_pillars")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PillarEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String purpose;

    @Column(name = "target_ratio", nullable = false, precision = 3, scale = 2)
    private BigDecimal targetRatio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PillarStatus status;

    @Column(name = "lock_no_reduce", nullable = false)
    private boolean lockNoReduce;

    @Builder(access = AccessLevel.PRIVATE)
    public PillarEntity(
            Long userId,
            String name,
            String purpose,
            BigDecimal targetRatio,
            boolean lockNoReduce,
            PillarStatus status
    ) {
        this.userId = userId;
        this.name = name;
        this.purpose = purpose;
        this.targetRatio = targetRatio;
        this.lockNoReduce = lockNoReduce;
        this.status = status;
    }

    public static PillarEntity create(
            Long userId,
            String name,
            String purpose,
            BigDecimal targetRatio,
            boolean lockNoReduce
    ) {
        return PillarEntity.builder()
                .userId(userId)
                .name(name)
                .purpose(purpose)
                .targetRatio(targetRatio)
                .lockNoReduce(lockNoReduce)
                .status(PillarStatus.ACTIVE)
                .build();
    }

    public static Pillar toDomain(PillarEntity entity) {
        return new Pillar(
                entity.getId(),
                entity.getUserId(),
                entity.getName(),
                entity.getPurpose(),
                entity.getTargetRatio(),
                entity.isLockNoReduce(),
                entity.getStatus()
        );
    }
}