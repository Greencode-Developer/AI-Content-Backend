package com.ai_content.followed_channel.service;

import com.ai_content.followed_channel.domain.FollowedChannel;
import com.ai_content.followed_channel.domain.FollowedChannelPlatform;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FollowedChannelRepository {

    List<FollowedChannel> findAllActiveForCollect();

    Optional<FollowedChannel> findActiveById(Long id);

    void updateLastFetchedAt(Long id, LocalDateTime fetchedAt);

    FollowedChannel create(Long userId,
                           FollowedChannelPlatform platform,
                           String channelUrl,
                           String displayName);

    Optional<FollowedChannel> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndChannelUrl(Long userId, String channelUrl);

    Page<FollowedChannel> findAllByUserId(Long userId, Pageable pageable);

    FollowedChannel update(FollowedChannel existing, String displayName, Boolean isActive);

    void softDelete(FollowedChannel existing);
}
