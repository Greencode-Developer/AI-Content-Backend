package com.ai_content.controller.persona;

import com.ai_content.config.web.annotation.CurrentUser;
import com.ai_content.controller.persona.request.CreatePersonaRequest;
import com.ai_content.controller.persona.request.UpdatePersonaRequest;
import com.ai_content.persona.command.DeletePersonaCommand;
import com.ai_content.persona.domain.Persona;
import com.ai_content.persona.service.PersonaService;
import com.ai_content.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/personas")
@RequiredArgsConstructor
public class PersonaController {

    private final PersonaService personaService;

    @PostMapping
    public Persona createPersona(@CurrentUser User user, @RequestBody @Valid CreatePersonaRequest request) {
        return personaService.createPersona(request.toCommand(user.id()));
    }

    @GetMapping
    public List<Persona> getPersonas(@CurrentUser User user) {
        return personaService.getPersonas(user.id());
    }

    @PutMapping("/{id}")
    public Persona updatePersona(
            @CurrentUser User user,
            @PathVariable Long id,
            @RequestBody @Valid UpdatePersonaRequest request
    ) {
        return personaService.updatePersona(request.toCommand(id, user.id()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePersona(@CurrentUser User user, @PathVariable Long id) {
        personaService.deletePersona(DeletePersonaCommand.of(id, user.id()));
    }
}
