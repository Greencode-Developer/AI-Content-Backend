package com.ai_content.pillar;

import com.ai_content.pillar.command.CreatePillarCommand;
import com.ai_content.pillar.domain.Pillar;
import com.ai_content.pillar.domain.PillarStatus;
import com.ai_content.pillar.service.PillarRepository;
import com.ai_content.pillar.service.PillarService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PillarServiceTest {
    @Mock
    private PillarRepository pillarRepository;

    @InjectMocks
    private PillarService pillarService;

    @Test
    void createPillar_shouldCreateActivePillarWithZeroTargetRatio() {
        // given
        CreatePillarCommand command = new CreatePillarCommand(
                1L,
                "Educational Content",
                "Share educational content",
                true
        );

        Pillar expected = Pillar.of(
                1L,
                1L,
                "Educational Content",
                "Share educational content",
                BigDecimal.valueOf(0),
                true,
                PillarStatus.ACTIVE);

        when(pillarRepository.createPillar(
                1L,
                "Educational Content",
                "Share educational content",
                true
        )).thenReturn(expected);

        // when
        Pillar result = pillarService.createPillar(command);

        // then
        assertThat(result).isSameAs(expected);
        assertThat(result.targetRatio()).isEqualByComparingTo(BigDecimal.ZERO);

        verify(pillarRepository).createPillar(
                1L,
                "Educational Content",
                "Share educational content",
                true
        );
    }


}
