package com.ai_content.infrastructure.ai;

import com.ai_content.idea.service.domain.AiIdeaGenerationResult;
import com.ai_content.idea.service.domain.GeneratedIdea;
import com.ai_content.idea.service.domain.IdeaGenerationInput;
import com.ai_content.idea.service.service.AiGenerationException;
import com.ai_content.idea.service.service.AiIdeaGenerator;
import com.ai_content.job.domain.AiGenerationMeta;
import com.fasterxml.jackson.core.JacksonException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

// Adapter to the Python AI service (prompt from Langfuse, LLM call, Langfuse tracing)
@Component
public class PythonAiClient implements AiIdeaGenerator {

    private static final int MAX_ERROR_BODY_LENGTH = 500;

    private final RestClient restClient;
    private final String provider;

    public PythonAiClient(
            RestClient.Builder builder,
            @Value("${ai.idea.base-url}") String baseUrl,
            @Value("${ai.idea.provider}") String provider,
            @Value("${ai.idea.connect-timeout:5s}") Duration connectTimeout,
            @Value("${ai.idea.read-timeout:90s}") Duration readTimeout
    ) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);

        this.restClient = builder
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
        this.provider = provider;
    }

    @Override
    public AiIdeaGenerationResult generate(Long jobId, Long userId, IdeaGenerationInput input) {
        GenerateIdeaRequest request = new GenerateIdeaRequest(
                input.topic(),
                input.brandName(),
                input.tone(),
                input.persona(),
                input.contentPillar(),
                userId,
                jobId
        );

        GenerateIdeaResponse response = call(request);
        if (response == null) {
            throw new AiGenerationException("AI service returned an empty body");
        }

        List<GeneratedIdea> ideas = response.ideas() == null ? List.of() : response.ideas().stream()
                .map(idea -> idea == null ? null
                        : new GeneratedIdea(idea.title(), idea.approachAngle(), idea.hookSentence(), idea.reason()))
                .toList();

        return new AiIdeaGenerationResult(
                ideas,
                new AiGenerationMeta(response.traceId(), response.model(), response.promptVersion())
        );
    }

    private GenerateIdeaResponse call(GenerateIdeaRequest request) {
        try {
            return restClient.post()
                    .uri(uri -> uri.path("/api/v1/ideas/generate")
                            .queryParam("provider", provider)
                            .build())
                    .body(request)
                    .retrieve()
                    .body(GenerateIdeaResponse.class);
        } catch (RestClientResponseException e) {
            throw new AiGenerationException(
                    "AI service returned " + e.getStatusCode().value() + ": " + truncate(e.getResponseBodyAsString()),
                    null, e);
        } catch (RestClientException e) {
            // A read timeout while reading the body surfaces as a plain RestClientException,
            // not ResourceAccessException, so look for the I/O cause instead of the exception type
            if (e instanceof ResourceAccessException || hasIoCause(e)) {
                throw new AiGenerationException("AI service unreachable or timed out: " + e.getMessage(), null, e);
            }
            throw new AiGenerationException("AI service returned an invalid response: " + e.getMessage(), null, e);
        }
    }

    // Jackson parse errors are IOExceptions too, but they mean a bad body, not a network problem
    private boolean hasIoCause(Throwable e) {
        for (Throwable cause = e.getCause(); cause != null; cause = cause.getCause()) {
            if (cause instanceof JacksonException) {
                return false;
            }
            if (cause instanceof IOException) {
                return true;
            }
        }
        return false;
    }

    private String truncate(String body) {
        if (body == null) {
            return "";
        }
        return body.length() <= MAX_ERROR_BODY_LENGTH ? body : body.substring(0, MAX_ERROR_BODY_LENGTH) + "...";
    }
}
