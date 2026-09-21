package com.ai_content.pillar.service;

import com.ai_content.common.error.CustomException;
import com.ai_content.common.error.ErrorCode;
import com.ai_content.pillar.command.CreatePillarCommand;
import com.ai_content.pillar.command.UpdatePillarCommand;
import com.ai_content.pillar.domain.Pillar;
import com.ai_content.user.domain.User;
import com.ai_content.user.service.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PillarService {
    private final PillarRepository pillarRepository;

    @Transactional
    public Pillar createPillar(CreatePillarCommand createPillar){
        return pillarRepository.createPillar(
                createPillar.userId(),
                createPillar.name(),
                createPillar.purpose(),
                createPillar.lockNoReduce());
    }

    public List<Pillar> getPillars(Long userId){
        return pillarRepository.getPillars(userId);
    }

    @Transactional
    public Pillar updatePillar(
            Long userId,
            Long pillarId,
            UpdatePillarCommand command
    ) {
        Pillar currentPillar = pillarRepository.getPillar(pillarId)
                .filter(Pillar::isActive)
                .orElseThrow(() -> new CustomException(ErrorCode.PILLAR_NOTFOUND));

        if (!currentPillar.userId().equals(userId)) {
            throw new CustomException(ErrorCode.PILLAR_NOTFOUND);
        }

        Pillar updatedPillar = new Pillar(
                currentPillar.id(),
                currentPillar.userId(),
                command.name() != null
                        ? command.name()
                        : currentPillar.name(),
                command.purpose() != null
                        ? command.purpose()
                        : currentPillar.purpose(),
                currentPillar.targetRatio(),
                command.lockNoReduce() != null
                        ? command.lockNoReduce()
                        : currentPillar.lockNoReduce(),
                currentPillar.status()

        );

        return pillarRepository.update(updatedPillar);
    }
    @Transactional
    public void deletePillar(Long userId, Long pillarId) {
        Pillar currentPillar = pillarRepository.getPillar(pillarId)
                .filter(Pillar::isActive)
                .orElseThrow(() -> new CustomException(ErrorCode.PILLAR_NOTFOUND));

        if (!currentPillar.userId().equals(userId)) {
            throw new CustomException(ErrorCode.PILLAR_NOTFOUND);
        }

        pillarRepository.delete(pillarId);
    }
}
