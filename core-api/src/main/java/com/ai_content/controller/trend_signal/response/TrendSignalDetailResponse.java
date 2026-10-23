package com.ai_content.controller.trend_signal.response;

import com.ai_content.trend_signal.domain.PostFormat;
import com.ai_content.trend_signal.domain.TrendSignal;
import com.ai_content.trend_signal.domain.TrendSignalWithChannel;

import java.time.LocalDateTime;
import java.util.Map;

public record TrendSignalDetailResponse(
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
        Map<String, Object> storyFormula,
        boolean isUsed,
        LocalDateTime createdAt
) {
    public static TrendSignalDetailResponse from(TrendSignalWithChannel item) {
        TrendSignal signal = item.trendSignal();
        return new TrendSignalDetailResponse(
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
                signal.storyFormula(),
                signal.isUsed(),
                signal.createdAt()
        );
    }
}
