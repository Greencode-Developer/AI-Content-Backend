package com.ai_content.job;

import com.ai_content.followed_channel.domain.FollowedChannel;
import com.ai_content.followed_channel.service.FollowedChannelRepository;
import com.ai_content.trend_signal.service.TrendSignalCollectionCommand;
import com.ai_content.trend_signal.service.TrendSignalCollectionPublisher;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CollectTrendSignalJob {

    private static final Logger log = LoggerFactory.getLogger(CollectTrendSignalJob.class);
    private static final long ADVISORY_LOCK_KEY = 7_100_001L;

    private final FollowedChannelRepository followedChannelRepository;
    private final TrendSignalCollectionPublisher trendSignalCollectionPublisher;
    private final DataSource dataSource;

    @Scheduled(cron = "${schedule.collect.cron}", zone = "Asia/Ho_Chi_Minh")
    public void run() {
        log.info("CollectTrendSignalJob started");

        try (Connection conn = dataSource.getConnection()) {
            if (!tryLock(conn)) {
                log.info("CollectTrendSignalJob skipped - another instance is running");
                return;
            }
            try {
                dispatch();
            } finally {
                unlock(conn);
                log.info("CollectTrendSignalJob finished");
            }
        } catch (Exception e) {
            log.error("CollectTrendSignalJob failed: {}", e.getMessage(), e);
        }
    }

    private void dispatch() {
        List<FollowedChannel> channels = followedChannelRepository.findAllActiveForCollect();
        log.info("Dispatching {} channels to collect", channels.size());

        int dispatched = 0;
        for (FollowedChannel channel : channels) {
            try {
                trendSignalCollectionPublisher.publish(
                        TrendSignalCollectionCommand.of(channel.id())
                );
                dispatched++;
                log.debug("Dispatched channel {}", channel.id());
            } catch (Exception e) {
                log.error("Failed to dispatch channel {}: {}", channel.id(), e.getMessage(), e);
            }
        }
        log.info("Dispatched {} channels successfully", dispatched);
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
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && !rs.getBoolean(1)) {
                    log.warn("pg_advisory_unlock returned false");
                }
            }
        } catch (Exception e) {
            log.warn("Failed to release advisory lock: {}", e.getMessage());
        }
    }
}