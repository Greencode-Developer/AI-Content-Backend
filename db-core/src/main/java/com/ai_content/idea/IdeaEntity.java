package com.ai_content.idea;

import com.ai_content.BaseEntity;
import com.ai_content.idea.service.domain.IdeaSource;
import com.ai_content.idea.service.domain.Platform;
import jakarta.persistence.*;

@Entity
@Table(name = "ideas")
public class IdeaEntity extends BaseEntity {

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Platform platform;

    @Column(nullable = false)
    private String title;

    private String approachAngle;

    private String hookSentence;

    private String reason;

    private Long pillarId;

    private Long jobId;

    private Long personaId;

    private Long trendSignalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IdeaSource source;

    @Column(nullable = false)
    private boolean isExploration;

    @Column(nullable = false)
    private boolean isUsed;
}