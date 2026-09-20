package com.ai_content.pillar;

import com.ai_content.pillar.domain.Pillar;
import com.ai_content.pillar.domain.PillarStatus;
import com.ai_content.pillar.service.PillarRepository;
import com.ai_content.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
@RequiredArgsConstructor
public class PillarCoreRepository implements PillarRepository {

    private final PillarJpaRepository pillarJpaRepository;

    @Override
    public Pillar createPillar(Long userId, String name, String purpose, BigDecimal targetRatio, boolean lockNoReduce) {
        PillarEntity pillar = pillarJpaRepository.save(PillarEntity.create(userId,name,purpose,targetRatio,lockNoReduce));
        return PillarEntity.toDomain(pillar);
    }
}
