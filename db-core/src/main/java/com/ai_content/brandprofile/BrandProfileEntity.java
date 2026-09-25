package com.ai_content.brandprofile;

import com.ai_content.brandprofile.domain.BrandProfile;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "brand_profiles")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BrandProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true, updatable = false)
    private Long userId;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "tone_of_voice", columnDefinition = "text")
    private String toneOfVoice;

    @Column(name = "forbidden_words", columnDefinition = "text")
    private String forbiddenWords;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "brand_colors", columnDefinition = "jsonb", nullable = false)
    private List<String> brandColors = new ArrayList<>();

    @Column(name = "completeness_pct", nullable = false)
    private int completenessPct;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static BrandProfileEntity createEmpty(Long userId) {
        BrandProfileEntity entity = new BrandProfileEntity();
        entity.userId = userId;
        entity.completenessPct = 0;
        return entity;
    }

    public void apply(BrandProfile profile) {
        this.description = profile.description();
        this.toneOfVoice = profile.toneOfVoice();
        this.forbiddenWords = profile.forbiddenWords();
        this.brandColors = new ArrayList<>(profile.brandColors());
        this.completenessPct = profile.completenessPct();
    }

    public BrandProfile toDomain() {
        return new BrandProfile(
                id,
                userId,
                description,
                toneOfVoice,
                forbiddenWords,
                brandColors,
                completenessPct,
                createdAt,
                updatedAt
        );
    }
}