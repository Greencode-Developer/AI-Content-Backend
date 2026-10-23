package com.ai_content.controller.trend_signal.response;

import com.ai_content.trend_signal.domain.PostFormat;
import com.ai_content.trend_signal.domain.TrendSignal;
import com.ai_content.trend_signal.domain.TrendSignalWithChannel;

import java.time.LocalDateTime;

public record TrendSignalSummaryResponse(
        Long id,
        Long followedChannelId,
        String channelName,
        String postId,
        String title,
        String url,
        LocalDateTime publishedAt,
        Integer likes,
        Integer comments,
        Integer shares,
        PostFormat postFormat,
        boolean isUsed,
        LocalDateTime createdAt
) {
    public static TrendSignalSummaryResponse from(TrendSignalWithChannel item) {
        TrendSignal signal = item.trendSignal();
        return new TrendSignalSummaryResponse(
                signal.id(),
                signal.followedChannelId(),
                item.channelDisplayName(),
                signal.postId(),
                signal.title(),
                signal.url(),
                signal.publishedAt(),
                signal.likes(),
                signal.comments(),
                signal.shares(),
                signal.postFormat(),
                signal.isUsed(),
                signal.createdAt()
        );
    }
}
