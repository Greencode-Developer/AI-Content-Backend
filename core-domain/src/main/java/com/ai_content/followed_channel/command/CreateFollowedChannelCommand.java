package com.ai_content.followed_channel.command;

import com.ai_content.followed_channel.domain.FollowedChannelPlatform;

public record CreateFollowedChannelCommand(
        Long userId,
        FollowedChannelPlatform platform,
        String channelUrl,
        String displayName
) {
    public static CreateFollowedChannelCommand of(
            Long userId,
            FollowedChannelPlatform platform,
            String channelUrl,
            String displayName
    ) {
        return new CreateFollowedChannelCommand(userId, platform, channelUrl, displayName);
    }
}
