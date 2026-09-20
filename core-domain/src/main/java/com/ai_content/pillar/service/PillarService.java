package com.ai_content.pillar.service;

import com.ai_content.pillar.command.CreatePillarCommand;
import com.ai_content.pillar.domain.Pillar;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PillarService {
    private final PillarRepository pillarRepository;

    public Pillar createPillar(CreatePillarCommand createPillar){
        return pillarRepository.createPillar(
                createPillar.userId(),
                createPillar.name(),
                createPillar.purpose(),
                createPillar.targetRatio(),
                createPillar.lockNoReduce());
    }
}
