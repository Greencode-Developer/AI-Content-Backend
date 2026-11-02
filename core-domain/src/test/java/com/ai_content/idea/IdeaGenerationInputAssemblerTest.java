package com.ai_content.idea;

import com.ai_content.brandprofile.domain.BrandProfile;
import com.ai_content.brandprofile.service.BrandProfileRepository;
import com.ai_content.common.error.CustomException;
import com.ai_content.common.error.ErrorCode;
import com.ai_content.idea.service.command.GenerateIdeaCommand;
import com.ai_content.idea.service.domain.IdeaGenerationInput;
import com.ai_content.idea.service.service.IdeaGenerationInputAssembler;
import com.ai_content.persona.domain.Persona;
import com.ai_content.persona.domain.PersonaStatus;
import com.ai_content.persona.service.PersonaRepository;
import com.ai_content.pillar.domain.Pillar;
import com.ai_content.pillar.domain.PillarStatus;
import com.ai_content.pillar.service.PillarRepository;
import com.ai_content.trend_signal.domain.TrendSignal;
import com.ai_content.trend_signal.domain.TrendSignalWithChannel;
import com.ai_content.trend_signal.service.TrendSignalRepository;
import com.ai_content.user.domain.User;
import com.ai_content.user.domain.UserRole;
import com.ai_content.user.domain.UserStatus;
import com.ai_content.user.service.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdeaGenerationInputAssemblerTest {

    @Mock UserRepository userRepository;
    @Mock BrandProfileRepository brandProfileRepository;
    @Mock PersonaRepository personaRepository;
    @Mock PillarRepository pillarRepository;
    @Mock TrendSignalRepository trendSignalRepository;

    @InjectMocks
    IdeaGenerationInputAssembler assembler;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        lenient().when(userRepository.findById(USER_ID)).thenReturn(Optional.of(
                User.of(USER_ID, "Shop A", "a@test.com", "pw", UserRole.USER, UserStatus.ACTIVE)));
        lenient().when(brandProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(
                new BrandProfile(1L, USER_ID, "desc", "thân thiện", null, List.of(), 50, null, null)));
    }

    @Test
    void assemble_withoutReferences_usesTopicUserNameAndTone() {
        IdeaGenerationInput input = assembler.assemble(USER_ID, new GenerateIdeaCommand("  Tết sale ", null, null, null));

        assertThat(input).isEqualTo(new IdeaGenerationInput(
                "Tết sale", "Shop A", "thân thiện", "", "", null, null, null));
    }

    @Test
    void assemble_withoutBrandProfile_sendsEmptyTone() {
        when(brandProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        IdeaGenerationInput input = assembler.assemble(USER_ID, GenerateIdeaCommand.empty());

        assertThat(input.tone()).isEmpty();
        assertThat(input.topic()).isEmpty();
    }

    @Test
    void assemble_describesPersonaAndPillar_andFallsBackToPillarName() {
        when(personaRepository.findActiveByIdAndUserId(2L, USER_ID)).thenReturn(Optional.of(
                Persona.of(2L, USER_ID, "Mẹ bỉm", "25-35", "Nội trợ", "Ít thời gian", null, "", PersonaStatus.ACTIVE)));
        when(pillarRepository.getPillar(3L)).thenReturn(Optional.of(pillar(USER_ID, PillarStatus.ACTIVE)));

        IdeaGenerationInput input = assembler.assemble(USER_ID, new GenerateIdeaCommand(null, 2L, 3L, null));

        assertThat(input.persona()).isEqualTo("Mẹ bỉm. Age: 25-35. Occupation: Nội trợ. Pain points: Ít thời gian");
        assertThat(input.contentPillar()).isEqualTo("Khuyến mãi. Purpose: Tăng doanh số");
        assertThat(input.topic()).isEqualTo("Khuyến mãi");
        assertThat(input.personaId()).isEqualTo(2L);
        assertThat(input.pillarId()).isEqualTo(3L);
    }

    @Test
    void assemble_usesTrendSignalTitleAsTopic_whenTopicMissing() {
        TrendSignal signal = mock(TrendSignal.class);
        when(signal.title()).thenReturn("Hot video 50% off");
        when(trendSignalRepository.findVisibleByIdAndUserId(4L, USER_ID))
                .thenReturn(Optional.of(new TrendSignalWithChannel(signal, "Channel A")));

        IdeaGenerationInput input = assembler.assemble(USER_ID, new GenerateIdeaCommand(" ", null, null, 4L));

        assertThat(input.topic()).isEqualTo("Hot video 50% off");
        assertThat(input.trendSignalId()).isEqualTo(4L);
    }

    @Test
    void assemble_throwsPersonaNotFound_whenPersonaNotOwnedOrDeleted() {
        when(personaRepository.findActiveByIdAndUserId(2L, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assembler.assemble(USER_ID, new GenerateIdeaCommand(null, 2L, null, null)))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PERSONA_NOT_FOUND);
    }

    @Test
    void assemble_throwsPillarNotFound_whenPillarBelongsToOtherUser() {
        when(pillarRepository.getPillar(3L)).thenReturn(Optional.of(pillar(99L, PillarStatus.ACTIVE)));

        assertThatThrownBy(() -> assembler.assemble(USER_ID, new GenerateIdeaCommand(null, null, 3L, null)))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PILLAR_NOTFOUND);
    }

    @Test
    void assemble_throwsPillarNotFound_whenPillarDeleted() {
        when(pillarRepository.getPillar(3L)).thenReturn(Optional.of(pillar(USER_ID, PillarStatus.DELETED)));

        assertThatThrownBy(() -> assembler.assemble(USER_ID, new GenerateIdeaCommand(null, null, 3L, null)))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PILLAR_NOTFOUND);
    }

    @Test
    void assemble_throwsTrendSignalNotFound_whenNotVisible() {
        when(trendSignalRepository.findVisibleByIdAndUserId(4L, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assembler.assemble(USER_ID, new GenerateIdeaCommand(null, null, null, 4L)))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TREND_SIGNAL_NOT_FOUND);
    }

    private Pillar pillar(Long ownerId, PillarStatus status) {
        return Pillar.of(3L, ownerId, "Khuyến mãi", "Tăng doanh số", BigDecimal.TEN, false, status);
    }
}
