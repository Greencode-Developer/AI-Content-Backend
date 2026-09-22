package com.ai_content.pillar.service;

import com.ai_content.pillar.domain.Pillar;

import java.util.List;
import java.util.Optional;

public interface PillarRepository {
    Pillar createPillar(Long userId,
                        String name,
                        String purpose,
                        Boolean lockNoReduce);

    List<Pillar> getPillars(Long userId);

    Optional<Pillar> getPillar(Long pillarId);

    Pillar update(Pillar updatedPillar);

    void delete(Long pillarId);

    List<Pillar> findAllByIdInAndUserId(List<Long> pillarIds, Long userId);

    List<Pillar>  saveAll(List<Pillar> updatedPillars);
}
