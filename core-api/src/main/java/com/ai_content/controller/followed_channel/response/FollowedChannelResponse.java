package com.ai_content.controller.followed_channel.response;

import com.ai_content.followed_channel.domain.FollowedChannel;
import com.ai_content.followed_channel.domain.FollowedChannelPlatform;
import com.ai_content.followed_channel.domain.FollowedChannelStatus;

import java.time.LocalDateTime;

public record FollowedChannelResponse(
        Long id,
        Long userId,
        FollowedChannelPlatform platform,
        String channelUrl,
        String displayName,
        boolean isActive,
        LocalDateTime lastFetchedAt,
        LocalDateTime createdAt,
        FollowedChannelStatus status
) {
    public static FollowedChannelResponse from(FollowedChannel domain) {
        return new FollowedChannelResponse(
                domain.id(),
                domain.userId(),
                domain.platform(),
                domain.channelUrl(),
                domain.displayName(),
                domain.isActive(),
                domain.lastFetchedAt(),
                domain.createdAt(),
                domain.status()
        );
    }
}
