package com.ai_content.trend_signal.domain;

public record TrendSignalWithChannel(
        TrendSignal trendSignal,
        String channelDisplayName
) {
}
