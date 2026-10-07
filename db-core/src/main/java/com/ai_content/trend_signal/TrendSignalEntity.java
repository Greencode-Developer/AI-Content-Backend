package com.ai_content.trend_signal;

import com.ai_content.BaseEntity;
import com.ai_content.trend_signal.converter.PostFormatConverter;
import com.ai_content.trend_signal.converter.TrendSignalStatusConverter;
import com.ai_content.trend_signal.domain.PostFormat;
import com.ai_content.trend_signal.domain.TrendSignal;
import com.ai_content.trend_signal.domain.TrendSignalStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
@Table(
        name = "trend_signals",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_trend_signals_channel_post",
                columnNames = {"followed_channel_id", "post_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrendSignalEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(
            name = "followed_channel_id",
            nullable = false,
            updatable = false
    )
    private Long followedChannelId;

    @Column(name = "post_id", nullable = false, updatable = false)
    private String postId;

    @Column(columnDefinition = "text")
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String url;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column
    private Integer likes;

    @Column
    private Integer comments;

    @Column
    private Integer shares;

    @Convert(converter = PostFormatConverter.class)
    @Column(name = "post_format", nullable = false, length = 20)
    private PostFormat postFormat;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "story_formula", columnDefinition = "jsonb")
    private Map<String, Object> storyFormula;

    @Column(name = "is_used", nullable = false)
    private boolean isUsed;

    @Convert(converter = TrendSignalStatusConverter.class)
    @Column(nullable = false, length = 20)
    private TrendSignalStatus status;

    @Builder(access = AccessLevel.PRIVATE)
    private TrendSignalEntity(
            Long userId,
            Long followedChannelId,
            String postId,
            String title,
            String url,
            LocalDateTime publishedAt,
            Integer likes,
            Integer comments,
            Integer shares,
            PostFormat postFormat
    ) {
        this.userId = userId;
        this.followedChannelId = followedChannelId;
        this.postId = postId;
        this.title = title;
        this.url = url;
        this.publishedAt = publishedAt;
        this.likes = likes;
        this.comments = comments;
        this.shares = shares;
        this.postFormat = postFormat;
        this.isUsed = false;
        this.status = TrendSignalStatus.ACTIVE;
    }

    public static TrendSignalEntity create(
            Long userId,
            Long followedChannelId,
            String postId,
            String title,
            String url,
            LocalDateTime publishedAt,
            Integer likes,
            Integer comments,
            Integer shares,
            PostFormat postFormat
    ) {
        return TrendSignalEntity.builder()
                .userId(userId)
                .followedChannelId(followedChannelId)
                .postId(postId)
                .title(title)
                .url(url)
                .publishedAt(publishedAt)
                .likes(likes)
                .comments(comments)
                .shares(shares)
                .postFormat(postFormat)
                .build();
    }

    public void updateMetrics(
            Integer likes,
            Integer comments,
            Integer shares
    ) {
        requireActive();
        this.likes = likes;
        this.comments = comments;
        this.shares = shares;
    }

    public void updateStoryFormula(Map<String, Object> storyFormula) {
        requireActive();
        this.storyFormula = storyFormula == null
                ? null
                : new LinkedHashMap<>(storyFormula);
    }

    public void markUsed() {
        requireActive();
        this.isUsed = true;
    }

    public void delete() {
        if (status == TrendSignalStatus.DELETED) {
            return;
        }
        this.status = TrendSignalStatus.DELETED;
        softDelete();
    }

    private void requireActive() {
        if (status != TrendSignalStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Cannot update a deleted trend signal"
            );
        }
    }

    public static TrendSignal toDomain(TrendSignalEntity entity) {
        return new TrendSignal(
                entity.getId(),
                entity.getUserId(),
                entity.getFollowedChannelId(),
                entity.getPostId(),
                entity.getTitle(),
                entity.getUrl(),
                entity.getPublishedAt(),
                entity.getLikes(),
                entity.getComments(),
                entity.getShares(),
                entity.getPostFormat(),
                entity.getStoryFormula() == null
                        ? null
                        : new LinkedHashMap<>(entity.getStoryFormula()),
                entity.isUsed(),
                entity.getCreatedAt(),
                entity.getStatus()
        );
    }
}