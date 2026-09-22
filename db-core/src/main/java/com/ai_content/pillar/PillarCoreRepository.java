package com.ai_content.pillar;

import com.ai_content.pillar.domain.Pillar;
import com.ai_content.pillar.service.PillarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PillarCoreRepository implements PillarRepository {

    private final PillarJpaRepository pillarJpaRepository;

    @Override
    public Pillar createPillar(Long userId, String name, String purpose, Boolean lockNoReduce) {
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

    @Override
    public Optional<Pillar> getPillar(Long pillarId) {

        return pillarJpaRepository.findById(pillarId)
                .map(PillarEntity::toDomain);
    }

    @Override
    public Pillar update(Pillar updatedPillar) {
        PillarEntity pillarEntity = pillarJpaRepository.findById(updatedPillar.id()).orElseThrow();
        pillarEntity.apply(updatedPillar);
        return PillarEntity.toDomain(pillarEntity);
    }

    @Override
    public void delete(Long pillarId) {
        PillarEntity pillarEntity = pillarJpaRepository.findById(pillarId).orElseThrow();
        pillarEntity.delete();
    }

    @Override
    public List<Pillar> findAllByIdInAndUserId(
            List<Long> pillarIds,
            Long userId
    ) {
        return pillarJpaRepository
                .findAllByIdInAndUserId(pillarIds, userId)
                .stream()
                .map(PillarEntity::toDomain)
                .toList();
    }

    @Override
    public List<Pillar> saveAll(List<Pillar> updatedPillars) {
        List<Long> pillarIds = updatedPillars.stream()
                .map(Pillar::id)
                .toList();

        List<PillarEntity> entities =
                pillarJpaRepository.findAllById(pillarIds);

        Map<Long, Pillar> pillarMap = updatedPillars.stream()
                .collect(Collectors.toMap(
                        Pillar::id,
                        Function.identity()
                ));

        entities.forEach(entity ->
                entity.apply(pillarMap.get(entity.getId()))
        );

        pillarJpaRepository.saveAll(entities);

        return entities.stream()
                .map(PillarEntity::toDomain)
                .toList();
    }
}
