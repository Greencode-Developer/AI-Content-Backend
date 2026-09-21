package com.ai_content.pillar.service;

import com.ai_content.pillar.domain.Pillar;
import com.ai_content.pillar.domain.PillarStatus;

import java.math.BigDecimal;
import java.util.List;

public interface PillarRepository {
    Pillar createPillar(Long userId,
                        String name,
                        String purpose,
                        boolean lockNoReduce);

    List<Pillar> getPillars(Long userId);
}
