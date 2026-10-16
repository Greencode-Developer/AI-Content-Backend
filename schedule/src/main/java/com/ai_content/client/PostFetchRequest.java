package com.ai_content.client;

public record PostFetchRequest (
    String channelUrl,
    int limit
) {}
