package com.ai_content.trend_signal.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TrendSignal(
        Long id,
        Long userId,
        String topic,
        String summary,
        Integer sourceCount,
        BigDecimal growthRate,
        Integer trendScore,
        LocalDateTime firstDetectedAt,
        LocalDateTime lastDetectedAt
) {
}