package com.ai_content.pillar.domain;

import java.math.BigDecimal;

public record Pillar(
        Long id,
        Long userId,
        String name,
        String purpose,
        BigDecimal targetRatio,
        boolean lockNoReduce,
        PillarStatus status
) {
    public static Pillar of(
            Long id,
            Long userId,
            String name,
            String purpose,
            BigDecimal targetRatio,
            boolean lockNoReduce,
            PillarStatus status
    ) {
        return new Pillar(
                id,
                userId,
                name,
                purpose,
                targetRatio,
                lockNoReduce,
                status
        );
    }

    public boolean isActive() {
        return this.status == PillarStatus.ACTIVE;
    }
}