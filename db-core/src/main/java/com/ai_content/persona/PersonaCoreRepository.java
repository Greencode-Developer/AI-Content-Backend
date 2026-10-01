package com.ai_content.persona;

import com.ai_content.persona.domain.Persona;
import com.ai_content.persona.service.PersonaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PersonaCoreRepository implements PersonaRepository {

    private final PersonaJpaRepository personaJpaRepository;

    @Override
    public Persona createPersona(Long userId, String name, String ageRange, String occupation, String painPoints, String desires, String typicalPhrases) {
        PersonaEntity persona = personaJpaRepository.save(PersonaEntity.create(userId, name, ageRange, occupation, painPoints, desires, typicalPhrases));
        return PersonaEntity.toDomain(persona);
    }
}
