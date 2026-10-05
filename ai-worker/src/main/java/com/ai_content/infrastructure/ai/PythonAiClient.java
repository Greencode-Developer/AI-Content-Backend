package com.ai_content.infrastructure.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PythonAiClient {

    private final RestClient restClient;

    public PythonAiClient(
            RestClient.Builder builder,
            @Value("${ai.python.base-url}") String baseUrl
    ) {
        this.restClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    public GenerateIdeaResponse generate(GenerateIdeaRequest request) {
        return restClient.post()
                .uri("/ideas/generate")
                .body(request)
                .retrieve()
                .body(GenerateIdeaResponse.class);
    }
}