package com.ai_content.persona;

import com.ai_content.BaseEntity;
import com.ai_content.persona.domain.Persona;
import com.ai_content.persona.domain.PersonaStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "personas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersonaEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "age_range", nullable = false)
    private String ageRange;

    @Column(name = "occupation", nullable = false)
    private String occupation;

    @Column(name = "pain_points", nullable = false, columnDefinition = "TEXT")
    private String painPoints;

    @Column(name = "desires", nullable = false, columnDefinition = "TEXT")
    private String desires;

    @Column(name = "typical_phrases", nullable = false, columnDefinition = "TEXT")
    private String typicalPhrases;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PersonaStatus status;

    @Builder(access = AccessLevel.PRIVATE)
    public PersonaEntity(
            Long userId,
            String name,
            String ageRange,
            String occupation,
            String painPoints,
            String desires,
            String typicalPhrases,
            PersonaStatus status
    ) {
        this.userId = userId;
        this.name = name;
        this.ageRange = ageRange;
        this.occupation = occupation;
        this.painPoints = painPoints;
        this.desires = desires;
        this.typicalPhrases = typicalPhrases;
        this.status = status;
    }

    public static PersonaEntity create(
            Long userId,
            String name,
            String ageRange,
            String occupation,
            String painPoints,
            String desires,
            String typicalPhrases
    ) {
        return PersonaEntity.builder()
                .userId(userId)
                .name(name)
                .ageRange(ageRange)
                .occupation(occupation)
                .painPoints(painPoints)
                .desires(desires)
                .typicalPhrases(typicalPhrases)
                .status(PersonaStatus.ACTIVE)
                .build();
    }

    public void update(String name, String ageRange, String occupation, String painPoints, String desires, String typicalPhrases) {
        this.name = name;
        this.ageRange = ageRange;
        this.occupation = occupation;
        this.painPoints = painPoints;
        this.desires = desires;
        this.typicalPhrases = typicalPhrases;
    }

    public void delete() {
        this.status = PersonaStatus.DELETED;
        super.softDelete();
    }

    public static Persona toDomain(PersonaEntity entity) {
        return Persona.of(
                entity.getId(),
                entity.getUserId(),
                entity.getName(),
                entity.getAgeRange(),
                entity.getOccupation(),
                entity.getPainPoints(),
                entity.getDesires(),
                entity.getTypicalPhrases(),
                entity.getStatus()
        );
    }
}
