package com.ai_content.followed_channel.command;

public record UpdateFollowedChannelCommand(
        Long id,
        Long userId,
        String displayName,
        Boolean isActive
) {
    public static UpdateFollowedChannelCommand of(
            Long id,
            Long userId,
            String displayName,
            Boolean isActive
    ) {
        return new UpdateFollowedChannelCommand(id, userId, displayName, isActive);
    }
}
