package com.ai_content.pillar.service;

import com.ai_content.pillar.command.CreatePillar;
import com.ai_content.pillar.domain.Pillar;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PillarService {
    private final PillarRepository pillarRepository;

    public Pillar createPillar(CreatePillar createPillar){
        return pillarRepository.createPillar(
                createPillar.userId(),
                createPillar.name(),
                createPillar.purpose(),
                createPillar.targetRatio(),
                createPillar.lockNoReduce(),
                createPillar.status());
    }
}
