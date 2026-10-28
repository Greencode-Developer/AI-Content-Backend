package com.ai_content.infrastructure.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// trace_id / model / prompt_version are optional until the AI service returns them
@JsonIgnoreProperties(ignoreUnknown = true)
public record GenerateIdeaResponse(
        @JsonProperty("ideas") List<IdeaResponse> ideas,
        @JsonProperty("trace_id") String traceId,
        @JsonProperty("model") String model,
        @JsonProperty("prompt_version") String promptVersion
) {}
