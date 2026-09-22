package com.ai_content.followed_channel;

import com.ai_content.common.error.CustomException;
import com.ai_content.common.error.ErrorCode;
import com.ai_content.followed_channel.domain.FollowedChannel;
import com.ai_content.followed_channel.domain.FollowedChannelPlatform;
import com.ai_content.followed_channel.service.FollowedChannelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FollowedChannelCoreRepository implements FollowedChannelRepository {

    private final FollowedChannelJpaRepository followedChannelJpaRepository;

    @Override
    @Transactional
    public FollowedChannel create(Long userId,
                                  FollowedChannelPlatform platform,
                                  String channelUrl,
                                  String displayName) {
        FollowedChannelEntity entity = followedChannelJpaRepository.save(
                FollowedChannelEntity.create(userId, platform, channelUrl, displayName)
        );
        return FollowedChannelEntity.toDomain(entity);
    }

    @Override
    public Optional<FollowedChannel> findByIdAndUserId(Long id, Long userId) {
        return followedChannelJpaRepository
                .findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .map(FollowedChannelEntity::toDomain);
    }

    @Override
    public boolean existsByUserIdAndChannelUrl(Long userId, String channelUrl) {
        return followedChannelJpaRepository
                .existsByUserIdAndChannelUrlAndDeletedAtIsNull(userId, channelUrl);
    }

    @Override
    public Page<FollowedChannel> findAllByUserId(Long userId, Pageable pageable) {
        return followedChannelJpaRepository
                .findAllByUserIdAndDeletedAtIsNull(userId, pageable)
                .map(FollowedChannelEntity::toDomain);
    }

    @Override
    @Transactional
    public FollowedChannel update(FollowedChannel existing, String displayName, Boolean isActive) {
        // findByIdAndDeletedAtIsNull để đảm bảo entity vẫn chưa bị xóa trong cùng transaction
        FollowedChannelEntity entity = followedChannelJpaRepository
                .findByIdAndDeletedAtIsNull(existing.id())
                .orElseThrow(() -> new CustomException(ErrorCode.FOLLOWED_CHANNEL_NOT_FOUND));
        entity.update(displayName, isActive);
        return FollowedChannelEntity.toDomain(entity);
    }

    @Override
    @Transactional
    public void softDelete(FollowedChannel existing) {
        FollowedChannelEntity entity = followedChannelJpaRepository
                .findByIdAndDeletedAtIsNull(existing.id())
                .orElseThrow(() -> new CustomException(ErrorCode.FOLLOWED_CHANNEL_NOT_FOUND));
        entity.delete();
    }}
