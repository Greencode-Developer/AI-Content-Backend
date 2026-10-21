package com.ai_content.trend_signal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrendSignalService {
    private final TrendSignalRepository trendSignalRepository;

    public void collect(Long followedChannelId, Long userId, List<CollectedPost> posts) {
        if (posts == null || posts.isEmpty()) {
            return;
        }
        trendSignalRepository.upsertAll(followedChannelId, userId, posts);
    }
}
