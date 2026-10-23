package com.ai_content.trend_signal.service;

import com.ai_content.common.error.CustomException;
import com.ai_content.common.error.ErrorCode;
import com.ai_content.trend_signal.domain.TrendSignalWithChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TrendSignalService {

    public static final Set<String> SORTABLE_PROPERTIES =
            Set.of("publishedAt", "likes", "comments", "shares");

    private final TrendSignalRepository trendSignalRepository;

    public void collect(Long followedChannelId, Long userId, List<CollectedPost> posts) {
        if (posts == null || posts.isEmpty()) {
            return;
        }
        trendSignalRepository.upsertAll(followedChannelId, userId, posts);
    }

    @Transactional(readOnly = true)
    public Page<TrendSignalWithChannel> search(Long userId, TrendSignalSearchCondition condition, Pageable pageable) {
        validateDateRange(condition);
        validateSort(pageable.getSort());
        return trendSignalRepository.search(userId, condition, pageable);
    }

    @Transactional(readOnly = true)
    public TrendSignalWithChannel getOne(Long id, Long userId) {
        return trendSignalRepository.findVisibleByIdAndUserId(id, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.TREND_SIGNAL_NOT_FOUND));
    }

    private void validateDateRange(TrendSignalSearchCondition condition) {
        if (condition.publishedFrom() != null
                && condition.publishedTo() != null
                && condition.publishedFrom().isAfter(condition.publishedTo())) {
            throw new CustomException(ErrorCode.TREND_SIGNAL_INVALID_DATE_RANGE);
        }
    }

    private void validateSort(Sort sort) {
        for (Sort.Order order : sort) {
            if (!SORTABLE_PROPERTIES.contains(order.getProperty())) {
                throw new CustomException(ErrorCode.TREND_SIGNAL_INVALID_SORT);
            }
        }
    }
}
