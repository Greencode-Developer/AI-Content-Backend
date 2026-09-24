package com.ai_content.followed_channel;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowedChannelJpaRepository extends JpaRepository<FollowedChannelEntity, Long> {

    Optional<FollowedChannelEntity> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);

    Optional<FollowedChannelEntity> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByUserIdAndChannelUrlAndDeletedAtIsNull(Long userId, String channelUrl);

    Page<FollowedChannelEntity> findAllByUserIdAndDeletedAtIsNull(Long userId, Pageable pageable);
}
