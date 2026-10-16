package com.ai_content.trend_signal;

import com.ai_content.trend_signal.service.CollectedPost;
import com.ai_content.trend_signal.service.TrendSignalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TrendSignalCoreRepository implements TrendSignalRepository{
    private  final TrendSignalJpaRepository trendSignalJpaRepository;

    @Override
    @Transactional
    public void upsertAll(Long followedChannelId, Long userId, List<CollectedPost> posts) {
        for (CollectedPost post : posts) {
            trendSignalJpaRepository.upsert(
                    userId,
                    followedChannelId,
                    post.postId(),
                    post.title(),
                    post.url(),
                    post.publishedAt(),
                    post.likes(),
                    post.comments(),
                    post.shares(),
                    post.postFormat().name()
            );
        }
    }
}
