package com.ai_content.pillar.service;

import com.ai_content.common.error.CustomException;
import com.ai_content.common.error.ErrorCode;
import com.ai_content.pillar.command.CreatePillarCommand;
import com.ai_content.pillar.command.TargetRatioItemCommand;
import com.ai_content.pillar.command.UpdatePillarCommand;
import com.ai_content.pillar.domain.Pillar;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


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

    @Transactional
    public List<Pillar> updateTargetRatios(
            Long userId,
            List<TargetRatioItemCommand> commands
    ) {
        Map<Long, BigDecimal> requestedRatios = commands.stream()
                .collect(Collectors.toMap(
                        TargetRatioItemCommand::pillarId,
                        TargetRatioItemCommand::targetRatio
                ));

        List<Pillar> pillars =
                pillarRepository.getPillars(userId);

        if (!pillars.stream()
                .map(Pillar::id)
                .collect(Collectors.toSet())
                .containsAll(requestedRatios.keySet())) {

            throw new CustomException(ErrorCode.PILLAR_NOTFOUND);
        }

        List<Pillar> updatedPillars = pillars.stream()
                .map(pillar -> {
                    BigDecimal targetRatio =
                            requestedRatios.getOrDefault(
                                    pillar.id(),
                                    pillar.targetRatio()
                            );

                    return Pillar.of(
                            pillar.id(),
                            pillar.userId(),
                            pillar.name(),
                            pillar.purpose(),
                            targetRatio,
                            pillar.lockNoReduce(),
                            pillar.status()
                    );
                })
                .toList();

        BigDecimal total = updatedPillars.stream()
                .map(Pillar::targetRatio)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (total.compareTo(BigDecimal.ONE) != 0) {
            throw new CustomException(
                    ErrorCode.InvalidTargetRatio
            );
        }

        return pillarRepository.saveAll(updatedPillars);
    }
}
