package com.ai_content.controller.persona;

import com.ai_content.config.web.annotation.CurrentUser;
import com.ai_content.controller.persona.request.CreatePersonaRequest;
import com.ai_content.persona.domain.Persona;
import com.ai_content.persona.service.PersonaService;
import com.ai_content.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/personas")
@RequiredArgsConstructor
public class PersonaController {
    private final PersonaService personaService;

    @PostMapping
    public Persona createPersona(@CurrentUser User user, @RequestBody @Valid CreatePersonaRequest request) {
        CreatePersonaRequest createPersonaRequest = CreatePersonaRequest.of(
                request.name(),
                request.ageRange(),
                request.occupation(),
                request.painPoints(),
                request.desires(),
                request.typicalPhrases()
        );
        return personaService.createPersona(createPersonaRequest.toCommand(user.id()));
    }
}
