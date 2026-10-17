package com.ai_content.job;

import com.ai_content.followed_channel.domain.FollowedChannel;
import com.ai_content.followed_channel.domain.FollowedChannelPlatform;
import com.ai_content.followed_channel.domain.FollowedChannelStatus;
import com.ai_content.followed_channel.service.FollowedChannelRepository;
import com.ai_content.trend_signal.service.TrendSignalCollectionCommand;
import com.ai_content.trend_signal.service.TrendSignalCollectionPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectTrendSignalJobTest {
    @Mock
    FollowedChannelRepository followedChannelRepository;
    @Mock
    TrendSignalCollectionPublisher trendSignalCollectionPublisher;
    @Mock
    DataSource dataSource;
    @Mock
    Connection connection;
    @Mock
    PreparedStatement lockStatement;
    @Mock
    PreparedStatement unlockStatement;
    @Mock
    ResultSet lockResultSet;

    @InjectMocks CollectTrendSignalJob job;

    @BeforeEach
    void setUp() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT pg_try_advisory_lock(?)"))
                .thenReturn(lockStatement);
        when(lockStatement.executeQuery()).thenReturn(lockResultSet);
    }

    private void givenLockAcquired() throws Exception {
        when(lockResultSet.next()).thenReturn(true);
        when(lockResultSet.getBoolean(1)).thenReturn(true);
        when(connection.prepareStatement("SELECT pg_advisory_unlock(?)"))
                .thenReturn(unlockStatement);
        when(unlockStatement.executeQuery()).thenReturn(mock(ResultSet.class));
    }

    private void givenLockNotAcquired() throws Exception {
        when(lockResultSet.next()).thenReturn(true);
        when(lockResultSet.getBoolean(1)).thenReturn(false);
    }

    private FollowedChannel makeChannel(Long id) {
        return new FollowedChannel(id, 99L, FollowedChannelPlatform.FANPAGE,
                "https://fb.com/channel" + id, "Channel" + id,
                true, null, LocalDateTime.now(), FollowedChannelStatus.ACTIVE);
    }

    @Test
    void run_whenLockAcquired_dispatchesAllChannelsAndUnlocks() throws Exception {
        givenLockAcquired();
        when(followedChannelRepository.findAllActiveForCollect())
                .thenReturn(List.of(makeChannel(1L), makeChannel(2L)));
        job.run();

        verify(trendSignalCollectionPublisher).publish(TrendSignalCollectionCommand.of(1L));
        verify(trendSignalCollectionPublisher).publish(TrendSignalCollectionCommand.of(2L));
        verify(unlockStatement).executeQuery();
    }

    @Test
    void run_whenLockNotAcquired_skipProcessing() throws Exception {
        givenLockNotAcquired();

        job.run();

        verifyNoInteractions(followedChannelRepository);
        verifyNoInteractions(trendSignalCollectionPublisher);
        verify(unlockStatement, never()).executeQuery();
    }

    @Test
    void run_whenOneChannelFailsToPublish_continuesWithOthers() throws Exception {
        givenLockAcquired();
        when(followedChannelRepository.findAllActiveForCollect())
                .thenReturn(List.of(makeChannel(1L), makeChannel(2L)));
        doThrow(new RuntimeException("SQS timeout"))
                .when(trendSignalCollectionPublisher).publish(TrendSignalCollectionCommand.of(1L));

        job.run();

        verify(trendSignalCollectionPublisher).publish(TrendSignalCollectionCommand.of(1L));
        verify(trendSignalCollectionPublisher).publish(TrendSignalCollectionCommand.of(2L));
    }

    @Test
    void run_whenNoChannels_doesNotPublish() throws Exception {
        givenLockAcquired();
        when(followedChannelRepository.findAllActiveForCollect()).thenReturn(List.of());

        job.run();

        verifyNoInteractions(trendSignalCollectionPublisher);
    }

}