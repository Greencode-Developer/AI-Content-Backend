package com.ai_content.infrastructure.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

// Body of POST /api/v1/ideas/generate on the Python AI service (snake_case).
// user_id / job_id let the AI service tag the Langfuse trace (user, session).
public record GenerateIdeaRequest(
        @JsonProperty("topic") String topic,
        @JsonProperty("brand_name") String brandName,
        @JsonProperty("tone") String tone,
        @JsonProperty("persona") String persona,
        @JsonProperty("content_pillar") String contentPillar,
        @JsonProperty("user_id") Long userId,
        @JsonProperty("job_id") Long jobId
) {}
