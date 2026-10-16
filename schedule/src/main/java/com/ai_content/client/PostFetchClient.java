package com.ai_content.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public PostFetchResponse fetch(String channelUrl, int limit) {
        log.debug("Fetching posts for channel: {}, limit: {}", channelUrl, limit);
        PostFetchResponse response = restClient.post()
                .uri("/posts/fetch")
                .body(new PostFetchRequest(channelUrl, limit))
                .retrieve()
                .body(PostFetchResponse.class);
        return response != null ? response : new PostFetchResponse(List.of());
    }
}
