package com.ai_content.trend_signal;

import com.ai_content.common.error.CustomException;
import com.ai_content.common.error.ErrorCode;
import com.ai_content.trend_signal.domain.PostFormat;
import com.ai_content.trend_signal.service.CollectedPost;
import com.ai_content.trend_signal.service.TrendSignalRepository;
import com.ai_content.trend_signal.service.TrendSignalSearchCondition;
import com.ai_content.trend_signal.service.TrendSignalService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    private static final TrendSignalSearchCondition NO_FILTER =
            new TrendSignalSearchCondition(null, null, null, null, null, null);

    @Test
    void search_withWhitelistedSort_delegatesToRepository() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "likes", "publishedAt"));
        trendSignalService.search(99L, NO_FILTER, pageable);
        verify(trendSignalRepository).search(99L, NO_FILTER, pageable);
    }

    @Test
    void search_withUnknownSortProperty_throwsInvalidSort() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by("userId"));
        assertThatThrownBy(() -> trendSignalService.search(99L, NO_FILTER, pageable))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.TREND_SIGNAL_INVALID_SORT);
        verifyNoInteractions(trendSignalRepository);
    }

    @Test
    void search_withFromAfterTo_throwsInvalidDateRange() {
        TrendSignalSearchCondition condition = new TrendSignalSearchCondition(
                null, null, null, LocalDate.of(2026, 10, 21), LocalDate.of(2026, 10, 20), null);
        assertThatThrownBy(() -> trendSignalService.search(99L, condition, PageRequest.of(0, 20)))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.TREND_SIGNAL_INVALID_DATE_RANGE);
        verifyNoInteractions(trendSignalRepository);
    }

    @Test
    void search_withSameFromAndTo_isAllowed() {
        LocalDate day = LocalDate.of(2026, 10, 21);
        TrendSignalSearchCondition condition = new TrendSignalSearchCondition(null, null, null, day, day, null);
        trendSignalService.search(99L, condition, PageRequest.of(0, 20));
        verify(trendSignalRepository).search(eq(99L), eq(condition), any());
    }

    @Test
    void searchCondition_blankKeyword_becomesNull_andKeywordIsTrimmed() {
        assertThat(new TrendSignalSearchCondition(null, null, null, null, null, "   ").keyword()).isNull();
        assertThat(new TrendSignalSearchCondition(null, null, null, null, null, "  hot  ").keyword()).isEqualTo("hot");
    }

    @Test
    void getOne_whenNotVisible_throwsNotFound() {
        when(trendSignalRepository.findVisibleByIdAndUserId(1L, 99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> trendSignalService.getOne(1L, 99L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.TREND_SIGNAL_NOT_FOUND);
    }
}
