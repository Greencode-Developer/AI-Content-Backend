package com.ai_content.trend_signal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;


public interface TrendSignalJpaRepository extends JpaRepository<TrendSignalEntity, Long> {

    List<TrendSignalEntity> findAllByFollowedChannelIdAndDeletedAtIsNull(Long followedChannelId);

    @Modifying(flushAutomatically = true)
    @Query(value = """
        INSERT INTO trend_signals
            (user_id, followed_channel_id, post_id,
             title, url, published_at,
             likes, comments, shares, post_format,
             story_formula, is_used, status, created_at, updated_at)
        VALUES
            (:userId, :followedChannelId, :postId,
             CAST(:title AS TEXT), :url, CAST(:publishedAt AS TIMESTAMP),
             CAST(:likes AS INTEGER), CAST(:comments AS INTEGER), CAST(:shares AS INTEGER),
             :postFormat,
             NULL, false, 'ACTIVE', NOW(), NOW())
        ON CONFLICT (followed_channel_id, post_id)
        DO UPDATE SET
            likes = COALESCE(EXCLUDED.likes, trend_signals.likes),
            comments = COALESCE(EXCLUDED.comments, trend_signals.comments),
            shares = COALESCE(EXCLUDED.shares, trend_signals.shares),
            updated_at = NOW()
        WHERE trend_signals.status = 'ACTIVE'
    """, nativeQuery = true)
    void upsert(
            @Param("userId") Long userId,
            @Param("followedChannelId") Long followedChannelId,
            @Param("postId") String postId,
            @Param("title") String title,
            @Param("url") String url,
            @Param("publishedAt") LocalDateTime publishedAt,
            @Param("likes") Integer likes,
            @Param("comments") Integer comments,
            @Param("shares") Integer shares,
            @Param("postFormat") String postFormat
    );
}
