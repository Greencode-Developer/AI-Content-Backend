package com.ai_content.job;

import com.ai_content.followed_channel.domain.FollowedChannel;
import com.ai_content.followed_channel.service.FollowedChannelRepository;
import com.ai_content.trend_signal.service.TrendSignalCollectionCommand;
import com.ai_content.trend_signal.service.TrendSignalCollectionPublisher;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CollectTrendSignalJob {

    private static final Logger log = LoggerFactory.getLogger(CollectTrendSignalJob.class);

    private final FollowedChannelRepository followedChannelRepository;
    private final TrendSignalCollectionPublisher trendSignalCollectionPublisher;

    @Scheduled(cron = "${schedule.collect.cron}", zone = "Asia/Ho_Chi_Minh")
    @SchedulerLock(
            name = "collectTrendSignal",
            lockAtLeastFor = "${schedule.collect.lock-at-least-for}",
            lockAtMostFor = "${schedule.collect.lock-at-most-for}"
    )
    public void run() {
        log.info("CollectTrendSignalJob started");
        try {
            dispatch();
        } catch (Exception e) {
            log.error("CollectTrendSignalJob failed: {}", e.getMessage(), e);
        } finally {
            log.info("CollectTrendSignalJob finished");
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
}
