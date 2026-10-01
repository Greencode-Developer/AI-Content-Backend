package com.ai_content.pillar;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonaJpaRepository extends JpaRepository<PersonaEntity,Long> {
    List<PillarEntity> findAllByPersonaId(Long personaId);

    List<PillarEntity> findAllByIdInAndUserId(
            List<Long> personaIds,
            Long userId
    );
}
