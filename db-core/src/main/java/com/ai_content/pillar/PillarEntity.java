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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

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
            UserEntity userEntity,
            String name,
            String purpose,
            BigDecimal targetRatio,
            boolean lockNoReduce,
            PillarStatus status
    ) {
        this.user = userEntity;
        this.name = name;
        this.purpose = purpose;
        this.targetRatio = targetRatio;
        this.lockNoReduce = lockNoReduce;
        this.status = status;
    }

    public static PillarEntity create(
            UserEntity userEntity,
            String name,
            String purpose,
            BigDecimal targetRatio,
            boolean lockNoReduce
    ) {
        return PillarEntity.builder()
                .userEntity(userEntity)
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
                entity.getUser().getId(),
                entity.getName(),
                entity.getPurpose(),
                entity.getTargetRatio(),
                entity.isLockNoReduce(),
                entity.getStatus()
        );
    }
}