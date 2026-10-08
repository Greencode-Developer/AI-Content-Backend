package com.ai_content.idea;

import com.ai_content.BaseEntity;
import com.ai_content.idea.service.domain.IdeaSource;
import com.ai_content.idea.service.domain.Platform;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "ideas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IdeaEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Platform platform;

    @Column(nullable = false)
    private String title;

    private String approachAngle;

    private String hookSentence;

    private String reason;

    @Column(name = "pillar_id")
    private Long pillarId;

    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "persona_id")
    private Long personaId;

    @Column(name = "trend_signal_id")
    private Long trendSignalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IdeaSource source;

    @Column(nullable = false)
    private boolean isExploration;

    @Column(nullable = false)
    private boolean isUsed;

    @Builder(access = AccessLevel.PRIVATE)
    private IdeaEntity(
            Long userId,
            Platform platform,
            String title,
            String approachAngle,
            String hookSentence,
            String reason,
            Long pillarId,
            Long jobId,
            Long personaId,
            Long trendSignalId,
            IdeaSource source,
            boolean isExploration
    ) {
        this.userId = userId;
        this.platform = platform;
        this.title = title;
        this.approachAngle = approachAngle;
        this.hookSentence = hookSentence;
        this.reason = reason;
        this.pillarId = pillarId;
        this.jobId = jobId;
        this.personaId = personaId;
        this.trendSignalId = trendSignalId;
        this.source = source;
        this.isExploration = isExploration;
        this.isUsed = false;
    }

    public static IdeaEntity create(
            Long userId,
            Platform platform,
            String title,
            String approachAngle,
            String hookSentence,
            String reason,
            Long pillarId,
            Long jobId,
            Long personaId,
            Long trendSignalId,
            IdeaSource source,
            boolean isExploration
    ) {
        return IdeaEntity.builder()
                .userId(userId)
                .platform(platform)
                .title(title)
                .approachAngle(approachAngle)
                .hookSentence(hookSentence)
                .reason(reason)
                .pillarId(pillarId)
                .jobId(jobId)
                .personaId(personaId)
                .trendSignalId(trendSignalId)
                .source(source)
                .isExploration(isExploration)
                .build();
    }

    public void markAsUsed() {
        this.isUsed = true;
    }
}