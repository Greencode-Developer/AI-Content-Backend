package com.ai_content.persona;

import com.ai_content.pillar.domain.Persona;
import com.ai_content.pillar.domain.PersonaStatus;
import com.ai_content.pillar.service.PersonaRepository;
import com.ai_content.user.domain.Persona;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
@RequiredArgsConstructor
public class PersonaCoreRepository implements PersonaRepository {

    private final PersonaJpaRepository personaJpaRepository;

    @Override
    public Pillar createPersona(Long userId, String name, String ageRange, String occupation, String painPoints, String desires, String typicalPhrases) {
        PersonaEntity persona = personaJpaRepository.save(PersonaEntity.create(userId, name, ageRange, occupation, painPoints, desires, typicalPhrases));
        return PersonaEntity.toDomain(persona);
    }
}
