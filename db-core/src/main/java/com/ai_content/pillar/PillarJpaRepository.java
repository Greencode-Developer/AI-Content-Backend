package com.ai_content.pillar;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PillarJpaRepository extends JpaRepository<PillarEntity,Long> {
    List<PillarEntity> findAllByUserId(Long userId);

    List<PillarEntity> findAllByIdInAndUserId(
            List<Long> pillarIds,
            Long userId
    );}
