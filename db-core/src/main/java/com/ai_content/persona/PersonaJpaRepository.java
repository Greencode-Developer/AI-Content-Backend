package com.ai_content.persona;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PersonaJpaRepository extends JpaRepository<PersonaEntity, Long> {
    List<PersonaEntity> findAllByUserId(Long userId);
}
