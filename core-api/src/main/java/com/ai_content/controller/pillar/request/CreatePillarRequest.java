package com.ai_content.controller.pillar.request;

import com.ai_content.pillar.command.CreatePillarCommand;
import com.ai_content.pillar.domain.PillarStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreatePillarRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 255, message = "Name must not exceed 255 characters")
        String name,

        @Size(max = 1000, message = "Purpose must not exceed 1000 characters")
        String purpose,

        boolean lockNoReduce
) {
    public static CreatePillarRequest of(
            String name,
            String purpose,
            boolean lockNoReduce
    ){
        return new CreatePillarRequest(name, purpose, lockNoReduce);
    }

    public CreatePillarCommand toCommand(Long userId){
        return CreatePillarCommand.of(userId,name,purpose,lockNoReduce);
    }
}
