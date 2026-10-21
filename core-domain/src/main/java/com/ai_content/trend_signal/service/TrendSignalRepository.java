package com.ai_content.trend_signal.service;

import java.util.List;

public interface TrendSignalRepository {
    void upsertAll(Long followedChannelId, Long userId, List<CollectedPost> posts);
}
