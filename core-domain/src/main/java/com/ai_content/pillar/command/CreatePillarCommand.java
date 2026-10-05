package com.ai_content.pillar.command;

import com.ai_content.pillar.domain.PillarStatus;

import java.math.BigDecimal;

public record CreatePillarCommand(
    Long userId,
    String name,
    String purpose,
    Boolean lockNoReduce){
    public static CreatePillarCommand of(Long userId,
                                         String name,
                                         String purpose,
                                         Boolean lockNoReduce){
        return new CreatePillarCommand(userId, name, purpose, lockNoReduce);
    }
}
