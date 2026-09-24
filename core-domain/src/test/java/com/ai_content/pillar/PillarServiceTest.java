package com.ai_content.pillar;

import com.ai_content.common.error.CustomException;
import com.ai_content.common.error.ErrorCode;
import com.ai_content.pillar.command.CreatePillarCommand;
import com.ai_content.pillar.command.TargetRatioItemCommand;
import com.ai_content.pillar.command.UpdatePillarCommand;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    @Test
    void getPillars_shouldReturnPillarsOfUser() {
        // given
        Long userId = 1L;

        List<Pillar> expected = List.of(
                Pillar.of(
                        1L,
                        userId,
                        "Education",
                        "Share knowledge",
                        BigDecimal.ZERO,
                        false,
                        PillarStatus.ACTIVE
                )
        );

        when(pillarRepository.getPillars(userId))
                .thenReturn(expected);

        // when
        List<Pillar> result = pillarService.getPillars(userId);

        // then
        assertThat(result).isEqualTo(expected);

        verify(pillarRepository).getPillars(userId);
    }

    @Test
    void updatePillar_shouldUpdatePillar() {
        // given
        Long userId = 1L;
        Long pillarId = 10L;

        Pillar current = Pillar.of(
                pillarId,
                userId,
                "Old name",
                "Old purpose",
                new BigDecimal("0.30"),
                false,
                PillarStatus.ACTIVE
        );

        UpdatePillarCommand command = new UpdatePillarCommand(
                "New name",
                "New purpose",
                true
        );

        Pillar updated = Pillar.of(
                pillarId,
                userId,
                "New name",
                "New purpose",
                new BigDecimal("0.30"),
                true,
                PillarStatus.ACTIVE
        );

        when(pillarRepository.getPillar(pillarId))
                .thenReturn(Optional.of(current));

        when(pillarRepository.update(any(Pillar.class)))
                .thenReturn(updated);

        // when
        Pillar result = pillarService.updatePillar(
                userId,
                pillarId,
                command
        );

        // then
        assertThat(result).isEqualTo(updated);

        verify(pillarRepository).getPillar(pillarId);
        verify(pillarRepository).update(any(Pillar.class));
    }

    @Test
    void updatePillar_shouldThrowWhenPillarBelongsToAnotherUser() {
        // given
        Long userId = 1L;
        Long anotherUserId = 2L;
        Long pillarId = 10L;

        Pillar current = Pillar.of(
                pillarId,
                anotherUserId,
                "Education",
                "Share knowledge",
                BigDecimal.ZERO,
                false,
                PillarStatus.ACTIVE
        );

        when(pillarRepository.getPillar(pillarId))
                .thenReturn(Optional.of(current));

        // when / then
        assertThatThrownBy(() ->
                pillarService.updatePillar(
                        userId,
                        pillarId,
                        new UpdatePillarCommand(
                                "New",
                                "New purpose",
                                true
                        )
                )
        )
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.PILLAR_NOTFOUND
                );

        verify(pillarRepository, never()).update(any());
    }

    @Test
    void updatePillar_shouldThrowWhenPillarNotFound() {
        // given
        Long userId = 1L;
        Long pillarId = 10L;

        when(pillarRepository.getPillar(pillarId))
                .thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() ->
                pillarService.updatePillar(
                        userId,
                        pillarId,
                        new UpdatePillarCommand(
                                "New",
                                "New purpose",
                                true
                        )
                )
        )
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.PILLAR_NOTFOUND
                );

        verify(pillarRepository, never()).update(any());
    }
    @Test
    void deletePillar_shouldDeleteWhenPillarBelongsToUser() {
        // given
        Long userId = 1L;
        Long pillarId = 10L;

        Pillar current = Pillar.of(
                pillarId,
                userId,
                "Education",
                "Share knowledge",
                BigDecimal.ZERO,
                false,
                PillarStatus.ACTIVE
        );

        when(pillarRepository.getPillar(pillarId))
                .thenReturn(Optional.of(current));

        // when
        pillarService.deletePillar(userId, pillarId);

        // then
        verify(pillarRepository).delete(pillarId);
    }
    @Test
    void deletePillar_shouldThrowWhenPillarBelongsToAnotherUser() {
        // given
        Long userId = 1L;
        Long anotherUserId = 2L;
        Long pillarId = 10L;

        Pillar current = Pillar.of(
                pillarId,
                anotherUserId,
                "Education",
                "Share knowledge",
                BigDecimal.ZERO,
                false,
                PillarStatus.ACTIVE
        );

        when(pillarRepository.getPillar(pillarId))
                .thenReturn(Optional.of(current));

        // when / then
        assertThatThrownBy(() ->
                pillarService.deletePillar(userId, pillarId)
        )
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.PILLAR_NOTFOUND
                );

        verify(pillarRepository, never()).delete(any());
    }
    @Test
    void updateTargetRatios_shouldThrowWhenTotalIsNotOne() {
        // given
        Long userId = 1L;

        List<TargetRatioItemCommand> commands = List.of(
                new TargetRatioItemCommand(
                        10L,
                        new BigDecimal("0.40")
                ),
                new TargetRatioItemCommand(
                        20L,
                        new BigDecimal("0.50")
                )
        );

        List<Pillar> pillars = List.of(
                Pillar.of(
                        10L,
                        userId,
                        "A",
                        "A",
                        new BigDecimal("0.30"),
                        false,
                        PillarStatus.ACTIVE
                ),
                Pillar.of(
                        20L,
                        userId,
                        "B",
                        "B",
                        new BigDecimal("0.30"),
                        false,
                        PillarStatus.ACTIVE
                ),
                Pillar.of(
                        30L,
                        userId,
                        "C",
                        "C",
                        new BigDecimal("0.40"),
                        false,
                        PillarStatus.ACTIVE
                )
        );

        when(pillarRepository.getPillars(userId))
                .thenReturn(pillars);

        // when / then
        assertThatThrownBy(() ->
                pillarService.updateTargetRatios(userId, commands)
        )
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.InvalidTargetRatio
                );

        verify(pillarRepository, never()).saveAll(anyList());
    }
}
