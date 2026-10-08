package com.ai_content.trend_signal.domain;

import java.time.LocalDateTime;
import java.util.Map;

public record TrendSignal(
        Long id,
        Long userId,
        Long followedChannelId,
        String postId,
        String title,
        String url,
        LocalDateTime publishedAt,
        Integer likes,
        Integer comments,
        Integer shares,
        PostFormat postFormat,
        Map<String, Object> storyFormula,
        boolean isUsed,
        LocalDateTime createdAt,
        TrendSignalStatus status
) {
}