package com.ai_content.infrastructure.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IdeaResponse(
        @JsonProperty("title") String title,
        @JsonProperty("approach_angle") String approachAngle,
        @JsonProperty("hook_sentence") String hookSentence,
        @JsonProperty("reason") String reason
) {}
