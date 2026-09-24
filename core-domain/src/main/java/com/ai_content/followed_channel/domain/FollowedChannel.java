package com.ai_content.followed_channel.domain;

import java.time.LocalDateTime;

public record FollowedChannel(
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
    public boolean isOwner(Long requestUserId) {
        return this.userId.equals(requestUserId);
    }

    public boolean isStatusActive() {
        return this.status == FollowedChannelStatus.ACTIVE;
    }
}
