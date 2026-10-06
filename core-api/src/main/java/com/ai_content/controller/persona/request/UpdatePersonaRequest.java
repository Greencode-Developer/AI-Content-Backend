package com.ai_content.controller.persona.request;

import com.ai_content.persona.command.UpdatePersonaCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePersonaRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 255, message = "Name must not exceed 255 characters")
        String name,

        @NotBlank(message = "Age range is required")
        @Size(max = 100, message = "Age range must not exceed 100 characters")
        String ageRange,

        @NotBlank(message = "Occupation is required")
        @Size(max = 255, message = "Occupation must not exceed 255 characters")
        String occupation,

        @NotBlank(message = "Pain points are required")
        String painPoints,

        @NotBlank(message = "Desires are required")
        String desires,

        @NotBlank(message = "Typical phrases are required")
        String typicalPhrases
) {
    public UpdatePersonaCommand toCommand(Long personaId, Long userId) {
        return UpdatePersonaCommand.of(personaId, userId, name, ageRange, occupation, painPoints, desires, typicalPhrases);
    }
}
