package com.ai_content.followed_channel.service;

import com.ai_content.common.error.CustomException;
import com.ai_content.common.error.ErrorCode;
import com.ai_content.followed_channel.command.CreateFollowedChannelCommand;
import com.ai_content.followed_channel.command.UpdateFollowedChannelCommand;
import com.ai_content.followed_channel.domain.FollowedChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FollowedChannelService {

    private final FollowedChannelRepository followedChannelRepository;

    @Transactional
    public FollowedChannel create(CreateFollowedChannelCommand command) {
        if (followedChannelRepository.existsByUserIdAndChannelUrl(
                command.userId(), command.channelUrl())) {
            throw new CustomException(ErrorCode.FOLLOWED_CHANNEL_DUPLICATE_URL);
        }
        return followedChannelRepository.create(
                command.userId(),
                command.platform(),
                command.channelUrl(),
                command.displayName()
        );
    }

    public Page<FollowedChannel> getAll(Long userId, Pageable pageable) {
        return followedChannelRepository.findAllByUserId(userId, pageable);
    }

    public FollowedChannel getOne(Long id, Long userId) {
        return followedChannelRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.FOLLOWED_CHANNEL_NOT_FOUND));
    }

    @Transactional
    public FollowedChannel update(UpdateFollowedChannelCommand command) {
        FollowedChannel channel = followedChannelRepository.findByIdAndUserId(command.id(), command.userId())
                .orElseThrow(() -> new CustomException(ErrorCode.FOLLOWED_CHANNEL_NOT_FOUND));
        return followedChannelRepository.update(
                channel,
                command.displayName(),
                command.isActive()
        );
    }

    @Transactional
    public void delete(Long id, Long userId) {
        FollowedChannel channel = followedChannelRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.FOLLOWED_CHANNEL_NOT_FOUND));
        followedChannelRepository.softDelete(channel);
    }
}
