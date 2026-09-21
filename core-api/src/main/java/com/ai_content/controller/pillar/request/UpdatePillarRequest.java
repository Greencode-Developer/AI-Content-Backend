package com.ai_content.controller.pillar.request;

import com.ai_content.pillar.command.UpdatePillarCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePillarRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 255, message = "Name must not exceed 255 characters")
        String name,

        @Size(max = 1000, message = "Purpose must not exceed 1000 characters")
        String purpose,

        Boolean lockNoReduce
) {
    public UpdatePillarCommand toCommand(UpdatePillarRequest request) {
        return UpdatePillarCommand.of(request.name,request.purpose,request.lockNoReduce);
    }
}