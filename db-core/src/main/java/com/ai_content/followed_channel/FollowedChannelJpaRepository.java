package com.ai_content.followed_channel;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FollowedChannelJpaRepository extends JpaRepository<FollowedChannelEntity, Long> {

    @Query("""
            SELECT e FROM FollowedChannelEntity e
            WHERE e.status = com.ai_content.followed_channel.domain.FollowedChannelStatus.ACTIVE
                    AND e.isActive = true
                    AND e.deletedAt IS NULL
            ORDER BY e.lastFetchedAt ASC NULLS FIRST
            """)
    List<FollowedChannelEntity> findAllActiveForCollect();

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE FollowedChannelEntity e
            SET e.lastFetchedAt = :fetchedAt,
                e.updatedAt = :fetchedAt
            WHERE e.id = :id
            """)
    void updateLastFetchedAt(@Param("id") Long id,
                             @Param("fetchedAt") LocalDateTime fetchedAt);

    Optional<FollowedChannelEntity> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);

    Optional<FollowedChannelEntity> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByUserIdAndChannelUrlAndDeletedAtIsNull(Long userId, String channelUrl);

    Page<FollowedChannelEntity> findAllByUserIdAndDeletedAtIsNull(Long userId, Pageable pageable);
}
