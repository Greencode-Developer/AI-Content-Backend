package com.ai_content.persona;

import com.ai_content.common.error.CustomException;
import com.ai_content.common.error.ErrorCode;
import com.ai_content.persona.domain.Persona;
import com.ai_content.persona.domain.PersonaStatus;
import com.ai_content.persona.service.PersonaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PersonaCoreRepository implements PersonaRepository {

    private final PersonaJpaRepository personaJpaRepository;

    @Override
    public Persona createPersona(Long userId, String name, String ageRange, String occupation, String painPoints, String desires, String typicalPhrases) {
        PersonaEntity persona = personaJpaRepository.save(
                PersonaEntity.create(userId, name, ageRange, occupation, painPoints, desires, typicalPhrases)
        );
        return PersonaEntity.toDomain(persona);
    }

    @Override
    public List<Persona> getPersonas(Long userId) {
        return personaJpaRepository.findAllByUserIdAndStatusNot(userId, PersonaStatus.DELETED)
                .stream()
                .map(PersonaEntity::toDomain)
                .toList();
    }

    @Override
    public Persona updatePersona(Long personaId, Long userId, String name, String ageRange, String occupation, String painPoints, String desires, String typicalPhrases) {
        PersonaEntity entity = personaJpaRepository.findByIdAndUserId(personaId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.PERSONA_NOT_FOUND));

        if (entity.getStatus() == PersonaStatus.DELETED) {
            throw new CustomException(ErrorCode.PERSONA_NOT_FOUND);
        }

        entity.update(name, ageRange, occupation, painPoints, desires, typicalPhrases);
        return PersonaEntity.toDomain(entity);
    }

    @Override
    public void deletePersona(Long personaId, Long userId) {
        PersonaEntity entity = personaJpaRepository.findByIdAndUserId(personaId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.PERSONA_NOT_FOUND));

        if (entity.getStatus() == PersonaStatus.DELETED) {
            throw new CustomException(ErrorCode.PERSONA_NOT_FOUND);
        }

        entity.delete();
    }
}
