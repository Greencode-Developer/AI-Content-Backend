package com.ai_content.controller.persona.request;

import com.ai_content.persona.command.CreatePersonaCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePersonaRequest(
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
    public static CreatePersonaRequest of(
            String name,
            String ageRange,
            String occupation,
            String painPoints,
            String desires,
            String typicalPhrases
    ) {
        return new CreatePersonaRequest(name, ageRange, occupation, painPoints, desires, typicalPhrases);
    }

    public CreatePersonaCommand toCommand(Long userId) {
        return CreatePersonaCommand.of(userId, name, ageRange, occupation, painPoints, desires, typicalPhrases);
    }
}
