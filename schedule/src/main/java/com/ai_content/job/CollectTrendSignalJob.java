package com.ai_content.job;

import com.ai_content.client.PostDto;
import com.ai_content.client.PostFetchClient;
import com.ai_content.client.PostFetchResponse;
import com.ai_content.followed_channel.domain.FollowedChannel;
import com.ai_content.followed_channel.service.FollowedChannelRepository;
import com.ai_content.trend_signal.domain.PostFormat;
import com.ai_content.trend_signal.service.CollectedPost;
import com.ai_content.trend_signal.service.TrendSignalService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Locale;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CollectTrendSignalJob {

    private static final Logger log = LoggerFactory.getLogger(CollectTrendSignalJob.class);
    private static final long ADVISORY_LOCK_KEY = 7_100_001L;

    private final FollowedChannelRepository followedChannelRepository;
    private final TrendSignalService trendSignalService;
    private final PostFetchClient postFetchClient;
    private final DataSource dataSource;

    @Value("${schedule.collect.post-limit:50}")
    private int postLimit;

    @Scheduled(cron = "${schedule.collect.cron}", zone = "Asia/Ho_Chi_Minh")
    public void run() {
        log.info("CollectTrendSignalJob started");

        try (Connection conn = dataSource.getConnection()) {
            if (!tryLock(conn)) {
                log.info("CollectTrendSignalJob skipped - another instance is running");
                return;
            }
            try {
                runBatch();
            } finally {
                unlock(conn);
                log.info("CollectTrendSignalJob finished");
            }
        } catch (Exception e) {
            log.error("CollectTrendSignalJob failed to acquire connection: {}", e.getMessage(), e);
        }
    }

    private boolean tryLock(Connection conn) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("SELECT pg_try_advisory_lock(?)")) {
            ps.setLong(1, ADVISORY_LOCK_KEY);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        }
    }

    private void unlock(Connection conn) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT pg_advisory_unlock(?)")) {
            ps.setLong(1, ADVISORY_LOCK_KEY);
            ps.execute();
        } catch (Exception e) {
            log.warn("Failed to release advisory lock: {}", e.getMessage());
        }
    }

    private void runBatch() {
        List<FollowedChannel> channels = followedChannelRepository.findAllActiveForCollect();
        log.info("Found {} active channels to collect", channels.size());

        for (FollowedChannel channel : channels) {
            processChannel(channel);
        }
    }

    private void processChannel(FollowedChannel channel) {
        try {
            PostFetchResponse response = postFetchClient.fetch(channel.channelUrl(), postLimit);

            List<CollectedPost> posts = response.posts().stream()
                    .filter(dto -> {
                        if (dto.postId() == null || dto.postId().isBlank()
                            || dto.url() == null || dto.url().isBlank()) {
                            log.warn("Channel {} - skipping post with missing postId or url", channel.id());
                            return false;
                        }
                        return true;
                    }).map(this::toCollectedPost).toList();
            trendSignalService.collect(channel.id(), channel.userId(), posts);
            followedChannelRepository.updateLastFetchedAt(channel.id(), LocalDateTime.now());

            log.info("Channel {} - collected {} posts", channel.id(), posts.size());
        } catch (Exception e) {
            log.error("Channel {} failed: {}", channel.id(), e.getMessage(), e);
        }
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
        if (raw == null) return PostFormat.TEXT;

        try {
            return PostFormat.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            log.warn("Unknown postFormat '{}', defaulting to TEXT", raw);
            return PostFormat.TEXT;
        }
    }
}