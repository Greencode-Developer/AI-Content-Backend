package com.ai_content.trend_signal;

import com.ai_content.followed_channel.domain.FollowedChannelStatus;
import com.ai_content.trend_signal.domain.PostFormat;
import com.ai_content.trend_signal.domain.TrendSignalStatus;
import com.ai_content.trend_signal.domain.TrendSignalWithChannel;
import com.ai_content.trend_signal.service.CollectedPost;
import com.ai_content.trend_signal.service.TrendSignalRepository;
import com.ai_content.trend_signal.service.TrendSignalSearchCondition;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.ai_content.followed_channel.QFollowedChannelEntity.followedChannelEntity;
import static com.ai_content.trend_signal.QTrendSignalEntity.trendSignalEntity;

@Repository
@RequiredArgsConstructor
public class TrendSignalCoreRepository implements TrendSignalRepository{
    private  final TrendSignalJpaRepository trendSignalJpaRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    @Transactional
    public void upsertAll(Long followedChannelId, Long userId, List<CollectedPost> posts) {
        for (CollectedPost post : posts) {
            trendSignalJpaRepository.upsert(
                    userId,
                    followedChannelId,
                    post.postId(),
                    post.title(),
                    post.url(),
                    post.publishedAt(),
                    post.likes(),
                    post.comments(),
                    post.shares(),
                    post.postFormat().name()
            );
        }
    }

    @Override
    public Page<TrendSignalWithChannel> search(Long userId, TrendSignalSearchCondition condition, Pageable pageable) {
        BooleanExpression[] where = {
                visibleTo(userId),
                followedChannelIdEq(condition.followedChannelId()),
                postFormatEq(condition.postFormat()),
                isUsedEq(condition.isUsed()),
                publishedFrom(condition.publishedFrom()),
                publishedTo(condition.publishedTo()),
                titleContains(condition.keyword())
        };

        List<TrendSignalWithChannel> content = selectWithChannel()
                .where(where)
                .orderBy(toOrderSpecifiers(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch()
                .stream()
                .map(TrendSignalCoreRepository::toTrendSignalWithChannel)
                .toList();

        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(trendSignalEntity.count())
                .from(trendSignalEntity)
                .join(followedChannelEntity)
                .on(followedChannelEntity.id.eq(trendSignalEntity.followedChannelId))
                .where(where);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Optional<TrendSignalWithChannel> findVisibleByIdAndUserId(Long id, Long userId) {
        Tuple row = selectWithChannel()
                .where(trendSignalEntity.id.eq(id), visibleTo(userId))
                .fetchOne();
        return Optional.ofNullable(row).map(TrendSignalCoreRepository::toTrendSignalWithChannel);
    }

    private JPAQuery<Tuple> selectWithChannel() {
        return jpaQueryFactory
                .select(trendSignalEntity, followedChannelEntity.displayName)
                .from(trendSignalEntity)
                .join(followedChannelEntity)
                .on(followedChannelEntity.id.eq(trendSignalEntity.followedChannelId));
    }

    // Owned by the user, not deleted, and its channel has not been unfollowed (deleted).
    // Paused channels (is_active = false) are still visible.
    private BooleanExpression visibleTo(Long userId) {
        return trendSignalEntity.userId.eq(userId)
                .and(trendSignalEntity.status.eq(TrendSignalStatus.ACTIVE))
                .and(followedChannelEntity.status.eq(FollowedChannelStatus.ACTIVE));
    }

    private BooleanExpression followedChannelIdEq(Long followedChannelId) {
        return followedChannelId == null ? null : trendSignalEntity.followedChannelId.eq(followedChannelId);
    }

    private BooleanExpression postFormatEq(PostFormat postFormat) {
        return postFormat == null ? null : trendSignalEntity.postFormat.eq(postFormat);
    }

    private BooleanExpression isUsedEq(Boolean isUsed) {
        return isUsed == null ? null : trendSignalEntity.isUsed.eq(isUsed);
    }

    private BooleanExpression publishedFrom(LocalDate from) {
        return from == null ? null : trendSignalEntity.publishedAt.goe(from.atStartOfDay());
    }

    private BooleanExpression publishedTo(LocalDate to) {
        return to == null ? null : trendSignalEntity.publishedAt.lt(to.plusDays(1).atStartOfDay());
    }

    private BooleanExpression titleContains(String keyword) {
        return keyword == null ? null : trendSignalEntity.title.containsIgnoreCase(keyword);
    }

    // Sort properties are whitelisted in TrendSignalService. id DESC is always appended
    // as a tie-breaker so paging stays stable when the sort values are equal.
    private OrderSpecifier<?>[] toOrderSpecifiers(Sort sort) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();
        for (Sort.Order order : sort) {
            ComparableExpressionBase<?> path = sortPath(order.getProperty());
            orders.add((order.isAscending() ? path.asc() : path.desc()).nullsLast());
        }
        orders.add(trendSignalEntity.id.desc());
        return orders.toArray(OrderSpecifier[]::new);
    }

    private ComparableExpressionBase<?> sortPath(String property) {
        return switch (property) {
            case "publishedAt" -> trendSignalEntity.publishedAt;
            case "likes" -> trendSignalEntity.likes;
            case "comments" -> trendSignalEntity.comments;
            case "shares" -> trendSignalEntity.shares;
            default -> throw new IllegalArgumentException("Unsupported sort property: " + property);
        };
    }

    private static TrendSignalWithChannel toTrendSignalWithChannel(Tuple row) {
        return new TrendSignalWithChannel(
                TrendSignalEntity.toDomain(row.get(trendSignalEntity)),
                row.get(followedChannelEntity.displayName)
        );
    }
}
