package com.ai_content.pillar.service;

import com.ai_content.pillar.domain.Pillar;
import com.ai_content.pillar.domain.PillarStatus;

import java.math.BigDecimal;

public interface PillarRepository {
    Pillar createPillar(Long userId,
                        String name,
                        String purpose,
                        BigDecimal targetRatio,
                        boolean lockNoReduce,
                        PillarStatus status);
}
