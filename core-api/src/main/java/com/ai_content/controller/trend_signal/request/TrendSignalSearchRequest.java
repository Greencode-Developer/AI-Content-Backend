package com.ai_content.controller.trend_signal.request;

import com.ai_content.trend_signal.domain.PostFormat;
import com.ai_content.trend_signal.service.TrendSignalSearchCondition;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Query params for GET /api/v1/trend-signals. All filters are optional.
 * from / to are inclusive dates on published_at (yyyy-MM-dd).
 */
public record TrendSignalSearchRequest(
        Long channelId,
        PostFormat format,
        Boolean isUsed,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @Size(max = 100, message = "keyword must be at most 100 characters") String keyword
) {
    public TrendSignalSearchCondition toCondition() {
        return new TrendSignalSearchCondition(channelId, format, isUsed, from, to, keyword);
    }
}
