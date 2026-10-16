package com.ai_content.job;

import com.ai_content.client.PostDto;
import com.ai_content.client.PostFetchClient;
import com.ai_content.client.PostFetchResponse;
import com.ai_content.followed_channel.domain.FollowedChannel;
import com.ai_content.followed_channel.domain.FollowedChannelPlatform;
import com.ai_content.followed_channel.domain.FollowedChannelStatus;
import com.ai_content.followed_channel.service.FollowedChannelRepository;
import com.ai_content.trend_signal.domain.PostFormat;
import com.ai_content.trend_signal.service.CollectedPost;
import com.ai_content.trend_signal.service.TrendSignalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectTrendSignalJobTest {

    @Mock FollowedChannelRepository followedChannelRepository;
    @Mock TrendSignalService trendSignalService;
    @Mock PostFetchClient postFetchClient;
    @Mock DataSource dataSource;
    @Mock Connection connection;

    // mock for acquire and release
    @Mock PreparedStatement lockStatement;
    @Mock PreparedStatement unlockStatement;
    @Mock ResultSet lockResultSet;

    @InjectMocks
    CollectTrendSignalJob job;

    @BeforeEach
    void setUp() throws Exception {
        // inject postLimit
        ReflectionTestUtils.setField(job, "postLimit", 50);

        when(dataSource.getConnection()).thenReturn(connection);
        // SQL acquire -> lockStatement, SQL, release -> unlockStatement
        when(connection.prepareStatement("SELECT pg_try_advisory_lock(?)"))
                .thenReturn(lockStatement);
        when(lockStatement.executeQuery()).thenReturn(lockResultSet);
    }

    private void givenLockAcquired() throws Exception {
        when(lockResultSet.next()).thenReturn(true);
        when(lockResultSet.getBoolean(1)).thenReturn(true);
        when(connection.prepareStatement("SELECT pg_advisory_unlock(?)"))
                .thenReturn(unlockStatement);
    }

    private void givenLockNotAcquired() throws Exception {
        when(lockResultSet.next()).thenReturn(true);
        when(lockResultSet.getBoolean(1)).thenReturn(false);
    }

    private FollowedChannel makeChannel(Long id) {
        return new FollowedChannel(
                id, 99L,
                FollowedChannelPlatform.FANPAGE,
                "https://fb.com/channel" + id,
                "Channel " + id,
                true, null,
                LocalDateTime.now(),
                FollowedChannelStatus.ACTIVE
        );
    }

    @Test
    void run_whenLockAcquired_processesChannelsAndReleasesLock() throws Exception {
        givenLockAcquired();

        FollowedChannel channel = makeChannel(1L);
        when(followedChannelRepository.findAllActiveForCollect()).thenReturn(List.of(channel));
        when(postFetchClient.fetch("https://fb.com/channel1", 50))
                .thenReturn(new PostFetchResponse(List.of(
                        new PostDto("p1", "Title", "https://fb.com/p1",
                                null, 10, 2, 1, "TEXT")
                )));
        job.run();

        verify(trendSignalService).collect(eq(1L), eq(99L), anyList());
        verify(followedChannelRepository).updateLastFetchedAt(eq(1L), any(LocalDateTime.class));
        verify(unlockStatement).execute();
    }

    @Test
    void run_whenLockNotAcquired_skipProcessing() throws Exception {
        givenLockNotAcquired();

        job.run();

        verifyNoInteractions(followedChannelRepository);
        verifyNoInteractions(trendSignalService);
        verify(unlockStatement, never()).execute();
    }

    @Test
    void run_whenOneChannelFails_otherChannelStillProcessed() throws Exception {
        givenLockAcquired();

        FollowedChannel channel1 = makeChannel(1L);
        FollowedChannel channel2 = makeChannel(2L);
        when(followedChannelRepository.findAllActiveForCollect()).
                thenReturn(List.of(channel1, channel2));
        when(postFetchClient.fetch(eq("https://fb.com/channel1"), eq(50)))
                .thenThrow(new RuntimeException("timeout"));
        when(postFetchClient.fetch(eq("https://fb.com/channel2"), eq(50)))
                .thenReturn(new PostFetchResponse(List.of()));
        job.run();

        verify(followedChannelRepository, never()).updateLastFetchedAt(eq(1L), any());
        verify(followedChannelRepository). updateLastFetchedAt(eq(2L), any(LocalDateTime.class));
    }

    @Test
    void run_whenChannelFails_lastFetchedAtNotUpdate() throws Exception {
        givenLockAcquired();

        FollowedChannel channel = makeChannel(1L);
        when(followedChannelRepository.findAllActiveForCollect()).thenReturn(List.of(channel));
        when(postFetchClient.fetch(anyString(), anyInt()))
                .thenThrow(new RuntimeException("Python service unavailable"));
        job.run();

        verify(trendSignalService, never()).collect(anyLong(), anyLong(), anyList());
        verify(followedChannelRepository, never()).updateLastFetchedAt(anyLong(), any());
    }

    @Test
    void run_postFormatLowercaseAndBlanks_normalizedCorrectly() throws Exception {
        givenLockAcquired();

        FollowedChannel channel = makeChannel(1L);
        when(followedChannelRepository.findAllActiveForCollect()).thenReturn(List.of(channel));
        when(postFetchClient.fetch(anyString(), anyInt()))
                .thenReturn(new PostFetchResponse((List.of(
                        new PostDto("p1", "Title", "https://fb.com/p1",
                                null, null, null, null, " image_text")
                ))));
        job.run();
        verify(trendSignalService).collect(eq(1L), eq(99L), argThat(posts ->
                posts.size() == 1 &&
                posts.get(0).postFormat() == PostFormat.IMAGE_TEXT
        ));
    }

    @Test
    void run_postsWithNullOrBlankRequiredFields_filtered() throws Exception {
        givenLockAcquired();

        FollowedChannel channel = makeChannel(1L);
        when(followedChannelRepository.findAllActiveForCollect()).thenReturn(List.of(channel));
        when(postFetchClient.fetch(anyString(), anyInt()))
                .thenReturn(new PostFetchResponse(List.of(
                        new PostDto(null, "Title", "https://fb.com/p1",
                                null, null, null, null, "TEXT"),
                        new PostDto(" ", "Title", "https://fb.com/p2",
                                null, null, null, null, "TEXT"),
                        new PostDto("p3", "Title", null,
                                null, null, null, null, "TEXT"),
                        new PostDto("p4", "Title", "https://fb.com/p4",
                                null, null, null, null, "TEXT")
                )));
        job.run();

        verify(trendSignalService).collect(eq(1L), eq(99L), argThat(posts ->
            posts.size() == 1 &&
            posts.get(0).postId().equals("p4")
        ));
    }
}