package com.ai_content.persona.service;

import com.ai_content.persona.domain.Persona;

import java.util.List;

public interface PersonaRepository {

    Persona createPersona(Long userId,
                          String name,
                          String ageRange,
                          String occupation,
                          String painPoints,
                          String desires,
                          String typicalPhrases);

    List<Persona> getPersonas(Long userId);

    Persona updatePersona(Long personaId,
                          Long userId,
                          String name,
                          String ageRange,
                          String occupation,
                          String painPoints,
                          String desires,
                          String typicalPhrases);

    void deletePersona(Long personaId, Long userId);
}
