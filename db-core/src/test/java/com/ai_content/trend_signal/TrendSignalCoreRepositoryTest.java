package com.ai_content.trend_signal;

import com.ai_content.config.PostgresTestContainerConfig;
import com.ai_content.followed_channel.FollowedChannelEntity;
import com.ai_content.followed_channel.FollowedChannelJpaRepository;
import com.ai_content.followed_channel.domain.FollowedChannelPlatform;
import com.ai_content.trend_signal.domain.PostFormat;
import com.ai_content.trend_signal.domain.TrendSignalStatus;
import com.ai_content.trend_signal.service.CollectedPost;
import com.ai_content.trend_signal.service.TrendSignalRepository;
import com.ai_content.user.UserEntity;
import com.ai_content.user.UserJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        TrendSignalCoreRepository.class,
        PostgresTestContainerConfig.class
})
@ActiveProfiles("test-integration")
class TrendSignalCoreRepositoryTest {

    @Autowired
    TrendSignalRepository trendSignalRepository;
    @Autowired
    TrendSignalJpaRepository trendSignalJpaRepository;
    @Autowired
    FollowedChannelJpaRepository followedChannelJpaRepository;
    @Autowired
    UserJpaRepository userJpaRepository;

    @PersistenceContext
    EntityManager entityManager;

    private Long userId;
    private Long channelId;

    @BeforeEach
    void setUp() {
        UserEntity user = userJpaRepository.saveAndFlush(
                UserEntity.createUser("Test User", "test@example.com", "hash")
        );
        userId = user.getId();

        FollowedChannelEntity channel = followedChannelJpaRepository.saveAndFlush(
                FollowedChannelEntity.create(userId, FollowedChannelPlatform.FANPAGE,
                        "https://fb.com/testchannel", "Test Channel")
        );
        channelId = channel.getId();
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void upsertAll_insertsNewPosts() {
        trendSignalRepository.upsertAll(channelId, userId, List.of(
                new CollectedPost("post1", "Title 1", "https://fb.com/p1",
                        LocalDateTime.now(), 10, 2, 1, PostFormat.TEXT),
                new CollectedPost("post2", "Title 2", "https://fb.com/p2",
                        LocalDateTime.now(), 20, 5, 3, PostFormat.IMAGE_TEXT)
        ));

        flushAndClear();

        List<TrendSignalEntity> saved = trendSignalJpaRepository
                .findAllByFollowedChannelIdAndDeletedAtIsNull(channelId);
        assertThat(saved).hasSize(2);
        assertThat(saved).extracting(TrendSignalEntity::getPostId)
                .containsExactlyInAnyOrder("post1", "post2");
    }

    @Test
    void upsertAll_onConflict_updatesMetricsOnly() {
        trendSignalRepository.upsertAll(channelId, userId, List.of(
                new CollectedPost("post1", "Original Title", "https://fb.com/p1",
                        LocalDateTime.now(), 10, 2, 1, PostFormat.TEXT)
        ));
        flushAndClear();

        trendSignalRepository.upsertAll(channelId, userId, List.of(
                new CollectedPost("post1", "Changed Title", "https://fb.com/p1",
                        LocalDateTime.now(), 999, 888, 777, PostFormat.TEXT)
        ));

        flushAndClear();

        TrendSignalEntity signal = trendSignalJpaRepository
                .findAllByFollowedChannelIdAndDeletedAtIsNull(channelId).get(0);

        assertThat(signal.getLikes()).isEqualTo(999);
        assertThat(signal.getComments()).isEqualTo(888);
        assertThat(signal.getShares()).isEqualTo(777);
        assertThat(signal.getTitle()).isEqualTo("Original Title");
    }

    @Test
    void upsertAll_onConflict_coalesceKeepsOldMetricsWhenNewIsNull() {
        trendSignalRepository.upsertAll(channelId, userId, List.of(
                new CollectedPost("post1", "Title", "https://fb.com/p1",
                        LocalDateTime.now(), 10, 2, 1, PostFormat.TEXT)
        ));
        flushAndClear();

        trendSignalRepository.upsertAll(channelId, userId, List.of(
                new CollectedPost("post1", "Title", "https://fb.com/p1",
                        LocalDateTime.now(), null, null, null, PostFormat.TEXT)
        ));
        flushAndClear();

        TrendSignalEntity signal = trendSignalJpaRepository
                .findAllByFollowedChannelIdAndDeletedAtIsNull(channelId).get(0);

        assertThat(signal.getLikes()).isEqualTo(10);
        assertThat(signal.getComments()).isEqualTo(2);
        assertThat(signal.getShares()).isEqualTo(1);
    }

    @Test
    void upsertAll_doesNotReviveSoftDeletedPost() {
        trendSignalRepository.upsertAll(channelId, userId, List.of(
                new CollectedPost("post1", "Title", "https://fb.com/p1",
                        LocalDateTime.now(), 10, 2, 1, PostFormat.TEXT)
        ));
        flushAndClear();

        TrendSignalEntity entity = trendSignalJpaRepository
                .findAllByFollowedChannelIdAndDeletedAtIsNull(channelId).get(0);
        entity.delete();
        flushAndClear();

        trendSignalRepository.upsertAll(channelId, userId, List.of(
                new CollectedPost("post1", "Title", "https://fb.com/p1",
                        LocalDateTime.now(), 99, 99, 99, PostFormat.TEXT)
        ));
        flushAndClear();

        TrendSignalEntity after = trendSignalJpaRepository.findById(entity.getId()).get();
        assertThat(after.getStatus()).isEqualTo(TrendSignalStatus.DELETED);
    }

    @Test
    void upsertAll_doesNotOverwriteIsUsedAndStoryFormula() {
        trendSignalRepository.upsertAll(channelId, userId, List.of(
                new CollectedPost("post1", "Title", "https://fb.com/p1",
                        LocalDateTime.now(), 10, 2, 1, PostFormat.TEXT)
        ));
        flushAndClear();

        TrendSignalEntity entity = trendSignalJpaRepository
                .findAllByFollowedChannelIdAndDeletedAtIsNull(channelId).get(0);
        entity.updateStoryFormula(Map.of("hook", "great hook"));
        entity.markUsed();
        trendSignalJpaRepository.saveAndFlush(entity);
        flushAndClear();

        trendSignalRepository.upsertAll(channelId, userId, List.of(
                new CollectedPost("post1", "Title", "https://fb.com/p1",
                        LocalDateTime.now(), 999, 999, 999, PostFormat.TEXT)
        ));
        flushAndClear();

        TrendSignalEntity after = trendSignalJpaRepository.findById(entity.getId()).get();
        assertThat(after.isUsed()).isTrue();
        assertThat(after.getStoryFormula()).containsEntry("hook", "great hook");
        assertThat(after.getLikes()).isEqualTo(999);
    }
}
