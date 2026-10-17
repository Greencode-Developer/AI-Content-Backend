package com.ai_content.application;

import com.ai_content.followed_channel.domain.FollowedChannel;
import com.ai_content.followed_channel.service.FollowedChannelRepository;
import com.ai_content.infrastructure.crawler.PostDto;
import com.ai_content.infrastructure.crawler.PostFetchClient;
import com.ai_content.infrastructure.crawler.PostFetchResponse;
import com.ai_content.trend_signal.domain.PostFormat;
import com.ai_content.trend_signal.service.CollectedPost;
import com.ai_content.trend_signal.service.TrendSignalService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CollectTrendSignalUseCase {
    private static final Logger log = LoggerFactory.getLogger(CollectTrendSignalUseCase.class);

    private final FollowedChannelRepository followedChannelRepository;
    private final PostFetchClient postFetchClient;
    private final TrendSignalService trendSignalService;

    @Value("${ai.collect.post-limit:50}")
    private int postLimit;

    public void execute(Long followedChannelId) {
        if (followedChannelId == null) {
            throw new IllegalArgumentException("followedChannelId is required");
        }

        FollowedChannel channel = followedChannelRepository.findActiveById(followedChannelId).orElse(null);

        if (channel == null) {
            log.info("Channel {} is no longer active or not found, skipping", followedChannelId);
            return;
        }

        PostFetchResponse response = postFetchClient.fetch(channel.channelUrl(), postLimit);

        List<CollectedPost> posts = response.posts().stream()
                .filter(dto -> {
                    if (dto.postId() == null || dto.postId().isBlank()
                        || dto.url() == null || dto.url().isBlank()) {
                        log.warn("Channel {} - skipping post with missing postId or url",
                                followedChannelId);
                        return false;
                    }
                    return true;
                })
                .map(this::toCollectedPost)
                .toList();
        trendSignalService.collect(channel.id(), channel.userId(), posts);
        followedChannelRepository.updateLastFetchedAt(channel.id(), LocalDateTime.now());
        log.info("Channel {} - collected {} posts", followedChannelId, posts.size());
    }

    private CollectedPost toCollectedPost(PostDto dto) {
        return new CollectedPost(
                dto.postId(),
                dto.title(),
                dto.url(),
                dto.publishedAt(),
                dto.likes(),
                dto.comments(),
                dto.shares(),
                parsePostFormat(dto.postFormat())
        );
    }

    private PostFormat parsePostFormat(String raw) {
        if (raw == null) {
            return PostFormat.TEXT;
        }
        try {
            return PostFormat.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            log.warn("Unknown postFormat '{}', defaulting to TEXT", raw);
            return PostFormat.TEXT;
        }
    }
}
