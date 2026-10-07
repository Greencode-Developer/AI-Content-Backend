package com.ai_content.trend_signal;

import com.ai_content.BaseEntity;
import com.ai_content.trend_signal.domain.TrendSignal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trend_signals")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrendSignalEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String topic;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "source_count", nullable = false)
    private Integer sourceCount;

    @Column(name = "growth_rate", precision = 19, scale = 4)
    private BigDecimal growthRate;

    @Column(name = "trend_score")
    private Integer trendScore;

    @Column(name = "first_detected_at", nullable = false)
    private LocalDateTime firstDetectedAt;

    @Column(name = "last_detected_at", nullable = false)
    private LocalDateTime lastDetectedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private TrendSignalEntity(
            Long userId,
            String topic,
            String summary,
            Integer sourceCount,
            BigDecimal growthRate,
            Integer trendScore,
            LocalDateTime firstDetectedAt,
            LocalDateTime lastDetectedAt
    ) {
        this.userId = userId;
        this.topic = topic;
        this.summary = summary;
        this.sourceCount = sourceCount;
        this.growthRate = growthRate;
        this.trendScore = trendScore;
        this.firstDetectedAt = firstDetectedAt;
        this.lastDetectedAt = lastDetectedAt;
    }

    public static TrendSignalEntity create(
            Long userId,
            String topic,
            String summary,
            Integer sourceCount,
            BigDecimal growthRate,
            Integer trendScore,
            LocalDateTime firstDetectedAt,
            LocalDateTime lastDetectedAt
    ) {
        return TrendSignalEntity.builder()
                .userId(userId)
                .topic(topic)
                .summary(summary)
                .sourceCount(sourceCount)
                .growthRate(growthRate)
                .trendScore(trendScore)
                .firstDetectedAt(firstDetectedAt)
                .lastDetectedAt(lastDetectedAt)
                .build();
    }

    public static TrendSignal toDomain(TrendSignalEntity entity) {
        return new TrendSignal(
                entity.getId(),
                entity.getUserId(),
                entity.getTopic(),
                entity.getSummary(),
                entity.getSourceCount(),
                entity.getGrowthRate(),
                entity.getTrendScore(),
                entity.getFirstDetectedAt(),
                entity.getLastDetectedAt()
        );
    }
}