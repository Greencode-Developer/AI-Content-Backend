package com.ai_content.persona.service;

import com.ai_content.persona.command.CreatePersonaCommand;
import com.ai_content.persona.domain.Persona;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PersonaService {
    private final PersonaRepository personaRepository;

    @Transactional
    public Persona createPersona(CreatePersonaCommand command) {
        return personaRepository.createPersona(
                command.userId(),
                command.name(),
                command.ageRange(),
                command.occupation(),
                command.painPoints(),
                command.desires(),
                command.typicalPhrases()
        );
    }
}
