package com.ai_content.infrastructure.ai;

import com.ai_content.idea.service.domain.AiIdeaGenerationResult;
import com.ai_content.idea.service.domain.GeneratedIdea;
import com.ai_content.idea.service.domain.IdeaGenerationInput;
import com.ai_content.idea.service.service.AiGenerationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Runs against a real local HTTP server so the timeouts and JSON mapping are exercised end to end
class PythonAiClientTest {

    private static final IdeaGenerationInput INPUT =
            new IdeaGenerationInput("Tết sale", "Shop A", "thân thiện", "Mẹ bỉm", "Khuyến mãi", 2L, 3L, null);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicReference<String> requestUri = new AtomicReference<>();
    private final AtomicReference<String> requestBody = new AtomicReference<>();

    private HttpServer server;
    private int status;
    private String responseBody;
    private long delayMillis;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", this::handle);
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void generate_sendsSnakeCaseRequestAndMapsResponse() throws Exception {
        respond(200, """
                {"ideas":[{"title":"Idea 1","approach_angle":"angle","hook_sentence":"hook","reason":"why",
                           "extra":"ignored"}],
                 "trace_id":"trace-abc","model":"claude","prompt_version":"3","usage":{"tokens":10}}
                """);

        AiIdeaGenerationResult result = client(Duration.ofSeconds(5)).generate(10L, 1L, INPUT);

        assertThat(requestUri.get()).isEqualTo("/api/v1/ideas/generate?provider=bedrock");
        JsonNode body = objectMapper.readTree(requestBody.get());
        assertThat(body.get("topic").asText()).isEqualTo("Tết sale");
        assertThat(body.get("brand_name").asText()).isEqualTo("Shop A");
        assertThat(body.get("tone").asText()).isEqualTo("thân thiện");
        assertThat(body.get("persona").asText()).isEqualTo("Mẹ bỉm");
        assertThat(body.get("content_pillar").asText()).isEqualTo("Khuyến mãi");
        assertThat(body.get("user_id").asLong()).isEqualTo(1L);
        assertThat(body.get("job_id").asLong()).isEqualTo(10L);

        assertThat(result.ideas()).containsExactly(new GeneratedIdea("Idea 1", "angle", "hook", "why"));
        assertThat(result.meta().traceId()).isEqualTo("trace-abc");
        assertThat(result.meta().model()).isEqualTo("claude");
        assertThat(result.meta().promptVersion()).isEqualTo("3");
    }

    @Test
    void generate_withoutTraceFields_returnsEmptyMeta() {
        respond(200, """
                {"ideas":[{"title":"Idea 1","approach_angle":"a","hook_sentence":"h","reason":"r"}]}
                """);

        AiIdeaGenerationResult result = client(Duration.ofSeconds(5)).generate(10L, 1L, INPUT);

        assertThat(result.ideas()).hasSize(1);
        assertThat(result.meta().traceId()).isNull();
        assertThat(result.meta().model()).isNull();
    }

    @Test
    void generate_whenServerError_throwsAiGenerationException() {
        respond(500, "{\"detail\":\"model overloaded\"}");

        assertThatThrownBy(() -> client(Duration.ofSeconds(5)).generate(10L, 1L, INPUT))
                .isInstanceOf(AiGenerationException.class)
                .hasMessageContaining("AI service returned 500")
                .hasMessageContaining("model overloaded");
    }

    @Test
    void generate_whenInvalidJson_throwsAiGenerationException() {
        respond(200, "not json");

        assertThatThrownBy(() -> client(Duration.ofSeconds(5)).generate(10L, 1L, INPUT))
                .isInstanceOf(AiGenerationException.class)
                .hasMessageContaining("invalid response");
    }

    @Test
    void generate_whenSlowerThanReadTimeout_throwsAiGenerationException() {
        respond(200, "{\"ideas\":[]}");
        delayMillis = 1_000;

        assertThatThrownBy(() -> client(Duration.ofMillis(200)).generate(10L, 1L, INPUT))
                .isInstanceOf(AiGenerationException.class)
                .hasMessageContaining("unreachable or timed out");
    }

    private PythonAiClient client(Duration readTimeout) {
        return new PythonAiClient(
                RestClient.builder(),
                "http://127.0.0.1:" + server.getAddress().getPort(),
                "bedrock",
                Duration.ofSeconds(1),
                readTimeout
        );
    }

    private void respond(int status, String body) {
        this.status = status;
        this.responseBody = body;
    }

    private void handle(HttpExchange exchange) throws IOException {
        requestUri.set(exchange.getRequestURI().toString());
        requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        } catch (IOException ignored) {
            // client already gave up (timeout test)
        }
    }
}
