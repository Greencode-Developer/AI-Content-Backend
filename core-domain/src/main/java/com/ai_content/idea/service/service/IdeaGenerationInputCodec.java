package com.ai_content.idea.service.service;

import com.ai_content.idea.service.domain.IdeaGenerationInput;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// Converts the input snapshot to/from the JSON stored in ai_jobs.input
@Component
@RequiredArgsConstructor
public class IdeaGenerationInputCodec {

    private final ObjectMapper objectMapper;

    public String toJson(IdeaGenerationInput input) {
        try {
            return objectMapper.writeValueAsString(input);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize idea generation input", e);
        }
    }

    public IdeaGenerationInput fromJson(String json) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException("Job has no input");
        }
        try {
            return objectMapper.readValue(json, IdeaGenerationInput.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Job input is not valid JSON", e);
        }
    }
}
