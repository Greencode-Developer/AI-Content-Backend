package com.ai_content.infrastructure.ai;

import java.util.List;

public record GenerateIdeaResponse(
        List<IdeaResponse> ideas
) {}
