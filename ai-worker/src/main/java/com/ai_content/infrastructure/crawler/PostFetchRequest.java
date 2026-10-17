package com.ai_content.infrastructure.crawler;

public record PostFetchRequest(
        String channelUrl,
        int limit
) {
}
