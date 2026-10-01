package com.ai_content.persona.service;

import com.ai_content.persona.domain.Persona;

public interface PersonaRepository {
    Persona createPersona(Long userId,
                          String name,
                          String ageRange,
                          String occupation,
                          String painPoints,
                          String desires,
                          String typicalPhrases);
}
