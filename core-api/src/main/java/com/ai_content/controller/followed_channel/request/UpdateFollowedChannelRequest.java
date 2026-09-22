package com.ai_content.controller.followed_channel.request;

import com.ai_content.followed_channel.command.UpdateFollowedChannelCommand;
import jakarta.validation.constraints.Size;

public record UpdateFollowedChannelRequest(

        @Size(max = 255, message = "Display name must not exceed 255 characters")
        String displayName,

        Boolean isActive
) {
    public UpdateFollowedChannelCommand toCommand(Long id, Long userId) {
        return UpdateFollowedChannelCommand.of(id, userId, displayName, isActive);
    }
}
