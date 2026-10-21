package com.ai_content.trend_signal.service;

public record TrendSignalCollectionCommand(
        Long followedChannelId
) {
    public static TrendSignalCollectionCommand of(Long followedChannelId) {
        return new TrendSignalCollectionCommand(followedChannelId);
    }
}
