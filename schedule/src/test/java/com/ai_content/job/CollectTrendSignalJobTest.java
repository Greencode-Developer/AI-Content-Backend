package com.ai_content.job;

import com.ai_content.followed_channel.domain.FollowedChannel;
import com.ai_content.followed_channel.domain.FollowedChannelPlatform;
import com.ai_content.followed_channel.domain.FollowedChannelStatus;
import com.ai_content.followed_channel.service.FollowedChannelRepository;
import com.ai_content.trend_signal.service.TrendSignalCollectionCommand;
import com.ai_content.trend_signal.service.TrendSignalCollectionPublisher;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectTrendSignalJobTest {
    @Mock
    FollowedChannelRepository followedChannelRepository;
    @Mock
    TrendSignalCollectionPublisher trendSignalCollectionPublisher;

    @InjectMocks CollectTrendSignalJob job;

    private FollowedChannel makeChannel(Long id) {
        return new FollowedChannel(id, 99L, FollowedChannelPlatform.FANPAGE,
                "https://fb.com/channel" + id, "Channel" + id,
                true, null, LocalDateTime.now(), FollowedChannelStatus.ACTIVE);
    }

    @Test
    void run_isGuardedBySchedulerLock() throws Exception {
        SchedulerLock lock = CollectTrendSignalJob.class
                .getMethod("run")
                .getAnnotation(SchedulerLock.class);

        assertThat(lock).isNotNull();
        assertThat(lock.name()).isEqualTo("collectTrendSignal");
        assertThat(lock.lockAtLeastFor()).isNotBlank();
        assertThat(lock.lockAtMostFor()).isNotBlank();
    }

    @Test
    void run_dispatchesAllChannels() {
        when(followedChannelRepository.findAllActiveForCollect())
                .thenReturn(List.of(makeChannel(1L), makeChannel(2L)));

        job.run();

        verify(trendSignalCollectionPublisher).publish(TrendSignalCollectionCommand.of(1L));
        verify(trendSignalCollectionPublisher).publish(TrendSignalCollectionCommand.of(2L));
    }

    @Test
    void run_whenOneChannelFailsToPublish_continuesWithOthers() {
        when(followedChannelRepository.findAllActiveForCollect())
                .thenReturn(List.of(makeChannel(1L), makeChannel(2L)));
        doThrow(new RuntimeException("SQS timeout"))
                .when(trendSignalCollectionPublisher).publish(TrendSignalCollectionCommand.of(1L));

        job.run();

        verify(trendSignalCollectionPublisher).publish(TrendSignalCollectionCommand.of(1L));
        verify(trendSignalCollectionPublisher).publish(TrendSignalCollectionCommand.of(2L));
    }

    @Test
    void run_whenNoChannels_doesNotPublish() {
        when(followedChannelRepository.findAllActiveForCollect()).thenReturn(List.of());

        job.run();

        verifyNoInteractions(trendSignalCollectionPublisher);
    }

    @Test
    void run_whenRepositoryFails_doesNotThrow() {
        when(followedChannelRepository.findAllActiveForCollect())
                .thenThrow(new RuntimeException("DB down"));

        assertThatCode(() -> job.run()).doesNotThrowAnyException();
        verifyNoInteractions(trendSignalCollectionPublisher);
    }
}
