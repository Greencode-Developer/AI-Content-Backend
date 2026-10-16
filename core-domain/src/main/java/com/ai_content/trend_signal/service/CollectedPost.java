package com.ai_content.trend_signal.service;

import com.ai_content.trend_signal.domain.PostFormat;

import java.time.LocalDateTime;

public record CollectedPost(
    String postId,
    String title,
    String url,
    LocalDateTime publishedAt,
    Integer likes,
    Integer comments,
    Integer shares,
    PostFormat postFormat
) {
    public CollectedPost {
        if (postFormat == null) {
            postFormat = PostFormat.TEXT;
        }
    }
}