package com.ai_content.infrastructure.crawler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.util.List;

@Component
public class PostFetchClient {
    private static final Logger log = LoggerFactory.getLogger(PostFetchClient.class);

    private final RestClient restClient;

    public PostFetchClient(
            RestClient.Builder builder,
            @Value("${ai.python.base-url}") String baseUrl
    ) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(30));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        this.restClient = builder
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .messageConverters(converters -> {
                    converters.removeIf(c -> c instanceof MappingJackson2HttpMessageConverter);
                    converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
                }).build();
    }

    public PostFetchResponse fetch(String channelUrl, int limit) {
        log.debug("Fetching posts for channels: {}, limit: {}", channelUrl, limit);
        PostFetchResponse response = restClient.post()
                .uri("/posts/fetch")
                .body(new PostFetchRequest(channelUrl, limit))
                .retrieve()
                .body(PostFetchResponse.class);
        return response != null ? response : new PostFetchResponse(List.of());
    }
}
