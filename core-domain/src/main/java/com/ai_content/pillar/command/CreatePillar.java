package com.ai_content.pillar.command;

import com.ai_content.pillar.domain.PillarStatus;

import java.math.BigDecimal;

public record CreatePillar (
    Long userId,
    String name,
    String purpose,
    BigDecimal targetRatio,
    boolean lockNoReduce,
    PillarStatus status){
}
