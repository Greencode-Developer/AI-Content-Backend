package com.ai_content.trend_signal.service;

import com.ai_content.trend_signal.domain.PostFormat;

import java.time.LocalDate;

/**
 * Optional filters for listing trend signals. A null field means "no filter".
 * publishedFrom / publishedTo are inclusive dates.
 */
public record TrendSignalSearchCondition(
        Long followedChannelId,
        PostFormat postFormat,
        Boolean isUsed,
        LocalDate publishedFrom,
        LocalDate publishedTo,
        String keyword
) {
    public TrendSignalSearchCondition {
        keyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
    }
}
