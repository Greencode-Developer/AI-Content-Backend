package com.ai_content.infrastructure.crawler;

import java.time.LocalDateTime;

public record PostDto(
        String postId,
        String title,
        String url,
        LocalDateTime publishedAt,
        Integer likes,
        Integer comments,
        Integer shares,
        String postFormat
) {
}
