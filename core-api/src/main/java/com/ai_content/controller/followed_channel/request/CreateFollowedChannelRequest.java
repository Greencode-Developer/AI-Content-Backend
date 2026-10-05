package com.ai_content.controller.followed_channel.request;

import com.ai_content.followed_channel.command.CreateFollowedChannelCommand;
import com.ai_content.followed_channel.domain.FollowedChannelPlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record CreateFollowedChannelRequest(

        @NotNull(message = "Platform is required")
        FollowedChannelPlatform platform,

        @NotBlank(message = "Channel URL is required")
        @URL(message = "Channel URL must be a valid URL")
        @Size(max = 500, message = "Channel URL must not exceed 500 characters")
        String channelUrl,

        @NotBlank(message = "Display name is required")
        @Size(max = 255, message = "Display name must not exceed 255 characters")
        String displayName
) {
    public CreateFollowedChannelCommand toCommand(Long userId) {
        return CreateFollowedChannelCommand.of(userId, platform, channelUrl, displayName);
    }
}
