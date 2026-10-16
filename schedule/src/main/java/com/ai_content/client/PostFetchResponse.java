package com.ai_content.client;

import java.util.List;

public record PostFetchResponse (
        List<PostDto> posts
) {
    public PostFetchResponse {
        if (posts == null) {
            posts = List.of();
        }
    }
}
