package com.ai_content.trend_signal;

import com.ai_content.trend_signal.domain.PostFormat;
import com.ai_content.trend_signal.service.CollectedPost;
import com.ai_content.trend_signal.service.TrendSignalRepository;
import com.ai_content.trend_signal.service.TrendSignalService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrendSignalServiceTest {

    @Mock
    TrendSignalRepository trendSignalRepository;

    @InjectMocks
    TrendSignalService trendSignalService;

    @Test
    void collect_withValidPosts_callsUpsertAll() {
        List<CollectedPost> posts = List.of(
                new CollectedPost("p1", "Title", "https://fb.com/p1",
                        null, 10, 2, 1, PostFormat.TEXT)
        );
        trendSignalService.collect(1L, 99L, posts);
        verify(trendSignalRepository).upsertAll(1L, 99L, posts);
    }

    @Test
    void collect_withEmptyList_doesNotCallRepository() {
        trendSignalService.collect(1L, 99L, List.of());
        verifyNoInteractions(trendSignalRepository);
    }

    @Test
    void collect_withNullList_doesNotCallRepository() {
        trendSignalService.collect(1L, 99L, null);
        verifyNoInteractions(trendSignalRepository);
    }
}
