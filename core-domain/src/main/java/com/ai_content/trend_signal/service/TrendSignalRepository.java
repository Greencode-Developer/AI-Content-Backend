package com.ai_content.trend_signal.service;

import com.ai_content.trend_signal.domain.TrendSignalWithChannel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TrendSignalRepository {
    void upsertAll(Long followedChannelId, Long userId, List<CollectedPost> posts);

    Page<TrendSignalWithChannel> search(Long userId, TrendSignalSearchCondition condition, Pageable pageable);

    Optional<TrendSignalWithChannel> findVisibleByIdAndUserId(Long id, Long userId);
}
