package com.ai_content.persona;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonaJpaRepository extends JpaRepository<PersonaEntity,Long> {
    List<PersonaEntity> findAllByPersonaId(Long personaId);

    List<PersonaEntity> findAllByIdInAndUserId(
            List<Long> personaIds,
            Long userId
    );
}
