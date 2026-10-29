package com.ai_content.idea.service.service;

import com.ai_content.brandprofile.domain.BrandProfile;
import com.ai_content.brandprofile.service.BrandProfileRepository;
import com.ai_content.common.error.CustomException;
import com.ai_content.common.error.ErrorCode;
import com.ai_content.idea.service.command.GenerateIdeaCommand;
import com.ai_content.idea.service.domain.IdeaGenerationInput;
import com.ai_content.persona.domain.Persona;
import com.ai_content.persona.service.PersonaRepository;
import com.ai_content.pillar.domain.Pillar;
import com.ai_content.pillar.domain.PillarStatus;
import com.ai_content.pillar.service.PillarRepository;
import com.ai_content.trend_signal.domain.TrendSignal;
import com.ai_content.trend_signal.service.TrendSignalRepository;
import com.ai_content.user.domain.User;
import com.ai_content.user.service.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/*
 * Builds the AI input from the user's data when the job is created.
 * Referenced persona/pillar/trend signal must belong to the user (404 otherwise).
 *
 * topic: request topic → trend signal title → pillar name
 * brandName: User.name (brand_profiles has no brand name column yet)
 */
@Component
@RequiredArgsConstructor
public class IdeaGenerationInputAssembler {

    private final UserRepository userRepository;
    private final BrandProfileRepository brandProfileRepository;
    private final PersonaRepository personaRepository;
    private final PillarRepository pillarRepository;
    private final TrendSignalRepository trendSignalRepository;

    public IdeaGenerationInput assemble(Long userId, GenerateIdeaCommand command) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOTFOUND));

        String tone = brandProfileRepository.findByUserId(userId)
                .map(BrandProfile::toneOfVoice)
                .orElse(null);

        Persona persona = command.personaId() == null ? null
                : personaRepository.findActiveByIdAndUserId(command.personaId(), userId)
                        .orElseThrow(() -> new CustomException(ErrorCode.PERSONA_NOT_FOUND));

        Pillar pillar = command.pillarId() == null ? null : findOwnedPillar(command.pillarId(), userId);

        TrendSignal trendSignal = command.trendSignalId() == null ? null
                : trendSignalRepository.findVisibleByIdAndUserId(command.trendSignalId(), userId)
                        .orElseThrow(() -> new CustomException(ErrorCode.TREND_SIGNAL_NOT_FOUND))
                        .trendSignal();

        return new IdeaGenerationInput(
                topic(command.topic(), trendSignal, pillar),
                user.name(),
                tone,
                describe(persona),
                describe(pillar),
                command.personaId(),
                command.pillarId(),
                command.trendSignalId()
        );
    }

    private Pillar findOwnedPillar(Long pillarId, Long userId) {
        return pillarRepository.getPillar(pillarId)
                .filter(pillar -> pillar.userId().equals(userId))
                .filter(pillar -> pillar.status() == PillarStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.PILLAR_NOTFOUND));
    }

    private String topic(String requested, TrendSignal trendSignal, Pillar pillar) {
        if (requested != null && !requested.isBlank()) {
            return requested;
        }
        if (trendSignal != null && trendSignal.title() != null) {
            return trendSignal.title();
        }
        return pillar != null ? pillar.name() : null;
    }

    private String describe(Persona persona) {
        if (persona == null) {
            return null;
        }
        List<String> parts = new ArrayList<>();
        add(parts, null, persona.name());
        add(parts, "Age", persona.ageRange());
        add(parts, "Occupation", persona.occupation());
        add(parts, "Pain points", persona.painPoints());
        add(parts, "Desires", persona.desires());
        add(parts, "Typical phrases", persona.typicalPhrases());
        return String.join(". ", parts);
    }

    private String describe(Pillar pillar) {
        if (pillar == null) {
            return null;
        }
        List<String> parts = new ArrayList<>();
        add(parts, null, pillar.name());
        add(parts, "Purpose", pillar.purpose());
        return String.join(". ", parts);
    }

    private void add(List<String> parts, String label, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        parts.add(label == null ? value.trim() : label + ": " + value.trim());
    }
}
