package com.ai_content.pillar;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PillarJpaRepository extends JpaRepository<PillarEntity,Long> {
    List<PillarEntity> findAllByUserId(Long userId);
}
