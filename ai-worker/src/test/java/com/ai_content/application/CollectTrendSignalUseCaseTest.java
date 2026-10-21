package com.ai_content.application;

import com.ai_content.followed_channel.domain.FollowedChannel;
import com.ai_content.followed_channel.domain.FollowedChannelPlatform;
import com.ai_content.followed_channel.domain.FollowedChannelStatus;
import com.ai_content.followed_channel.service.FollowedChannelRepository;
import com.ai_content.infrastructure.crawler.PostDto;
import com.ai_content.infrastructure.crawler.PostFetchClient;
import com.ai_content.infrastructure.crawler.PostFetchResponse;
import com.ai_content.trend_signal.domain.PostFormat;
import com.ai_content.trend_signal.service.TrendSignalService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectTrendSignalUseCaseTest {

    @Mock FollowedChannelRepository followedChannelRepository;
    @Mock PostFetchClient postFetchClient;
    @Mock TrendSignalService trendSignalService;

    @InjectMocks CollectTrendSignalUseCase useCase;

    private FollowedChannel makeChannel(Long id) {
        return new FollowedChannel(id, 99L, FollowedChannelPlatform.FANPAGE,
                "https://fb.com/channel" + id, "Channel " + id,
                true, null, LocalDateTime.now(), FollowedChannelStatus.ACTIVE);
    }

    @Test
    void execute_whenChannelNotFound_skipsWithoutCallingCrawler() {
        when(followedChannelRepository.findActiveById(1L)).thenReturn(Optional.empty());

        useCase.execute(1L);

        verifyNoInteractions(postFetchClient);
        verifyNoInteractions(trendSignalService);
        verify(followedChannelRepository, never()).updateLastFetchedAt(anyLong(), any());
    }

    @Test
    void execute_happyPath_fetchUpsertAndUpdateLastFetchedAt() {
        ReflectionTestUtils.setField(useCase, "postLimit", 50);
        FollowedChannel channel = makeChannel(1L);
        when(followedChannelRepository.findActiveById(1L)).thenReturn(Optional.of(channel));
        when(postFetchClient.fetch("https://fb.com/channel1", 50))
                .thenReturn(new PostFetchResponse(List.of(
                        new PostDto("p1", "Title", "https://fb.com/p1",
                                null, 10, 2, 1, "TEXT")
                )));

        useCase.execute(1L);

        verify(trendSignalService).collect(eq(1L), eq(99L), anyList());
        verify(followedChannelRepository).updateLastFetchedAt(eq(1L), any(LocalDateTime.class));
    }

    @Test
    void execute_whenCrawlerFails_throwsAndDoesNotUpdateLastFetchedAt() {
        ReflectionTestUtils.setField(useCase, "postLimit", 50);
        FollowedChannel channel = makeChannel(1L);
        when(followedChannelRepository.findActiveById(1L)).thenReturn(Optional.of(channel));
        when(postFetchClient.fetch(anyString(), anyInt()))
                .thenThrow(new RuntimeException("crawler timeout"));

        assertThatThrownBy(() -> useCase.execute(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("crawler timeout");

        verifyNoInteractions(trendSignalService);
        verify(followedChannelRepository, never()).updateLastFetchedAt(anyLong(), any());
    }

    @Test
    void execute_whenUpsertFails_throwsAndDoesNotUpdateLastFetchedAt() {
        ReflectionTestUtils.setField(useCase, "postLimit", 50);
        FollowedChannel channel = makeChannel(1L);
        when(followedChannelRepository.findActiveById(1L)).thenReturn(Optional.of(channel));
        when(postFetchClient.fetch(anyString(), anyInt()))
                .thenReturn(new PostFetchResponse(List.of(
                        new PostDto("p1", "Title", "https://fb.com/p1",
                                null, null, null, null, "TEXT")
                )));
        doThrow(new RuntimeException("DB error"))
                .when(trendSignalService).collect(anyLong(), anyLong(), anyList());

        assertThatThrownBy(() -> useCase.execute(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error");

        verify(followedChannelRepository, never()).updateLastFetchedAt(anyLong(), any());
    }

    @Test
    void execute_postsWithMissingPostIdOrUrl_filtered() {
        ReflectionTestUtils.setField(useCase, "postLimit", 50);
        FollowedChannel channel = makeChannel(1L);
        when(followedChannelRepository.findActiveById(1L)).thenReturn(Optional.of(channel));
        when(postFetchClient.fetch(anyString(), anyInt()))
                .thenReturn(new PostFetchResponse(List.of(
                        new PostDto(null, "Title", "https://fb.com/p1", null, null, null, null, "TEXT"),
        new PostDto(" ", "Title", "https://fb.com/p2", null, null, null, null, "TEXT"),
        new PostDto("p3", "Title", null, null, null, null, null, "TEXT"),
        new PostDto("p4", "Title", "https://fb.com/p4", null, null, null, null, "TEXT")
                  )));

        useCase.execute(1L);

        verify(trendSignalService).collect(eq(1L), eq(99L), argThat(posts ->
                posts.size() == 1 && posts.get(0).postId().equals("p4")
        ));
    }

    @Test
    void execute_unknownPostFormat_defaultsToText() {
        ReflectionTestUtils.setField(useCase, "postLimit", 50);
        FollowedChannel channel = makeChannel(1L);
        when(followedChannelRepository.findActiveById(1L)).thenReturn(Optional.of(channel));
        when(postFetchClient.fetch(anyString(), anyInt()))
                .thenReturn(new PostFetchResponse(List.of(
                        new PostDto("p1", "Title", "https://fb.com/p1",
                                null, null, null, null, "UNKNOWN_FORMAT")
                )));

        useCase.execute(1L);

        verify(trendSignalService).collect(eq(1L), eq(99L), argThat(posts ->
                posts.size() == 1 && posts.get(0).postFormat() == PostFormat.TEXT
        ));
    }

    @Test
    void execute_nullFollowedChannelId_throwsIllegalArgument() {
        assertThatThrownBy(() -> useCase.execute(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("followedChannelId is required");

        verifyNoInteractions(followedChannelRepository);
        verifyNoInteractions(postFetchClient);
    }
}

