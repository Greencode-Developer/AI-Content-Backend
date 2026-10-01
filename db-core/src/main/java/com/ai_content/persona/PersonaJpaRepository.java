package com.ai_content.persona;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PersonaJpaRepository extends JpaRepository<PersonaEntity, Long> {
    List<PersonaEntity> findAllByUserIdAndStatusNot(Long userId, com.ai_content.persona.domain.PersonaStatus status);
    Optional<PersonaEntity> findByIdAndUserId(Long id, Long userId);
}
