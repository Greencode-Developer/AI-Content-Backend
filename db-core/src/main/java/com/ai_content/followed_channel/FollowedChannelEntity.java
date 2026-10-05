package com.ai_content.followed_channel;

import com.ai_content.BaseEntity;
import com.ai_content.followed_channel.domain.FollowedChannel;
import com.ai_content.followed_channel.domain.FollowedChannelPlatform;
import com.ai_content.followed_channel.domain.FollowedChannelStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "followed_channels",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_followed_channels_user_url",
                columnNames = {"user_id", "channel_url"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FollowedChannelEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FollowedChannelPlatform platform;

    @Column(name = "channel_url", nullable = false, length = 500)
    private String channelUrl;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "last_fetched_at")
    private LocalDateTime lastFetchedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FollowedChannelStatus status;

    @Builder(access = AccessLevel.PRIVATE)
    private FollowedChannelEntity(
            Long userId,
            FollowedChannelPlatform platform,
            String channelUrl,
            String displayName,
            boolean isActive,
            FollowedChannelStatus status
    ) {
        this.userId = userId;
        this.platform = platform;
        this.channelUrl = channelUrl;
        this.displayName = displayName;
        this.isActive = isActive;
        this.status = status;
    }

    public static FollowedChannelEntity create(
            Long userId,
            FollowedChannelPlatform platform,
            String channelUrl,
            String displayName
    ) {
        return FollowedChannelEntity.builder()
                .userId(userId)
                .platform(platform)
                .channelUrl(channelUrl)
                .displayName(displayName)
                .isActive(true)
                .status(FollowedChannelStatus.ACTIVE)
                .build();
    }

    public void update(String displayName, Boolean isActive) {
        if (displayName != null) {
            this.displayName = displayName;
        }
        if (isActive != null) {
            this.isActive = isActive;
        }
    }

    public void delete() {
        this.status = FollowedChannelStatus.DELETED;
        softDelete();
    }

    public static FollowedChannel toDomain(FollowedChannelEntity entity) {
        return new FollowedChannel(
                entity.getId(),
                entity.getUserId(),
                entity.getPlatform(),
                entity.getChannelUrl(),
                entity.getDisplayName(),
                entity.isActive(),
                entity.getLastFetchedAt(),
                entity.getCreatedAt(),
                entity.getStatus()
        );
    }
}
