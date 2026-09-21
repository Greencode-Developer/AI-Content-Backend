package com.ai_content.pillar;

import com.ai_content.pillar.domain.Pillar;
import com.ai_content.pillar.domain.PillarStatus;
import com.ai_content.pillar.service.PillarRepository;
import com.ai_content.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PillarCoreRepository implements PillarRepository {

    private final PillarJpaRepository pillarJpaRepository;

    @Override
    public Pillar createPillar(Long userId, String name, String purpose, boolean lockNoReduce) {
        PillarEntity pillar = pillarJpaRepository.save(PillarEntity.create(userId,name,purpose,lockNoReduce));
        return PillarEntity.toDomain(pillar);
    }

    @Override
    public List<Pillar> getPillars(Long userId) {

        return pillarJpaRepository.findAllByUserId(userId)
                .stream()
                .map(PillarEntity::toDomain)
                .toList();
    }
}
