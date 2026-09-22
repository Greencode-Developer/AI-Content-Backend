package com.ai_content.followed_channel;

import com.ai_content.common.error.CustomException;
import com.ai_content.common.error.ErrorCode;
import com.ai_content.followed_channel.command.CreateFollowedChannelCommand;
import com.ai_content.followed_channel.command.UpdateFollowedChannelCommand;
import com.ai_content.followed_channel.domain.FollowedChannel;
import com.ai_content.followed_channel.domain.FollowedChannelPlatform;
import com.ai_content.followed_channel.domain.FollowedChannelStatus;
import com.ai_content.followed_channel.service.FollowedChannelRepository;
import com.ai_content.followed_channel.service.FollowedChannelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowedChannelServiceTest {

    @Mock
    private FollowedChannelRepository followedChannelRepository;

    @InjectMocks
    private FollowedChannelService followedChannelService;

    // ─── Fixtures ────────────────────────────────────────────────────────────

    private static final Long USER_ID    = 1L;
    private static final Long OTHER_USER = 2L;
    private static final Long CHANNEL_ID = 10L;
    private static final String CHANNEL_URL  = "https://www.facebook.com/testpage";
    private static final String DISPLAY_NAME = "Test Page";

    private FollowedChannel activeChannel() {
        return new FollowedChannel(
                CHANNEL_ID,
                USER_ID,
                FollowedChannelPlatform.FANPAGE,
                CHANNEL_URL,
                DISPLAY_NAME,
                true,
                null,
                LocalDateTime.now(),
                FollowedChannelStatus.ACTIVE
        );
    }

    // ─── create ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("create()")
    class Create {

        private CreateFollowedChannelCommand command;

        @BeforeEach
        void setUp() {
            command = CreateFollowedChannelCommand.of(
                    USER_ID,
                    FollowedChannelPlatform.FANPAGE,
                    CHANNEL_URL,
                    DISPLAY_NAME
            );
        }

        @Test
        @DisplayName("새 채널 follow 성공 시 저장된 도메인 객체를 반환한다")
        void create_success_returnsCreatedChannel() {
            FollowedChannel expected = activeChannel();

            when(followedChannelRepository.existsByUserIdAndChannelUrl(USER_ID, CHANNEL_URL))
                    .thenReturn(false);
            when(followedChannelRepository.create(USER_ID, FollowedChannelPlatform.FANPAGE, CHANNEL_URL, DISPLAY_NAME))
                    .thenReturn(expected);

            FollowedChannel result = followedChannelService.create(command);

            assertThat(result).isSameAs(expected);
            assertThat(result.userId()).isEqualTo(USER_ID);
            assertThat(result.channelUrl()).isEqualTo(CHANNEL_URL);
            assertThat(result.isActive()).isTrue();
            assertThat(result.status()).isEqualTo(FollowedChannelStatus.ACTIVE);

            verify(followedChannelRepository).existsByUserIdAndChannelUrl(USER_ID, CHANNEL_URL);
            verify(followedChannelRepository).create(USER_ID, FollowedChannelPlatform.FANPAGE, CHANNEL_URL, DISPLAY_NAME);
        }

        @Test
        @DisplayName("이미 follow한 URL이면 FOLLOWED_CHANNEL_DUPLICATE_URL 예외를 던진다")
        void create_duplicateUrl_throwsDuplicateException() {
            when(followedChannelRepository.existsByUserIdAndChannelUrl(USER_ID, CHANNEL_URL))
                    .thenReturn(true);

            assertThatThrownBy(() -> followedChannelService.create(command))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.FOLLOWED_CHANNEL_DUPLICATE_URL);

            verify(followedChannelRepository, never()).create(any(), any(), any(), any());
        }
    }

    // ─── getAll ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getAll()")
    class GetAll {

        @Test
        @DisplayName("사용자의 followed channel 목록을 페이지로 반환한다")
        void getAll_returnsPageOfUserChannels() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<FollowedChannel> expected = new PageImpl<>(List.of(activeChannel()));

            when(followedChannelRepository.findAllByUserId(USER_ID, pageable))
                    .thenReturn(expected);

            Page<FollowedChannel> result = followedChannelService.getAll(USER_ID, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).userId()).isEqualTo(USER_ID);
            verify(followedChannelRepository).findAllByUserId(USER_ID, pageable);
        }

        @Test
        @DisplayName("followed channel이 없으면 빈 페이지를 반환한다")
        void getAll_noChannels_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 20);
            when(followedChannelRepository.findAllByUserId(USER_ID, pageable))
                    .thenReturn(Page.empty());

            Page<FollowedChannel> result = followedChannelService.getAll(USER_ID, pageable);

            assertThat(result.getContent()).isEmpty();
        }
    }

    // ─── getOne ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getOne()")
    class GetOne {

        @Test
        @DisplayName("본인 채널 조회 성공")
        void getOne_ownerRequest_returnsChannel() {
            when(followedChannelRepository.findByIdAndUserId(CHANNEL_ID, USER_ID))
                    .thenReturn(Optional.of(activeChannel()));

            FollowedChannel result = followedChannelService.getOne(CHANNEL_ID, USER_ID);

            assertThat(result.id()).isEqualTo(CHANNEL_ID);
            assertThat(result.userId()).isEqualTo(USER_ID);
        }

        @Test
        @DisplayName("존재하지 않는 ID이면 FOLLOWED_CHANNEL_NOT_FOUND 예외를 던진다")
        void getOne_notFound_throwsNotFoundException() {
            when(followedChannelRepository.findByIdAndUserId(CHANNEL_ID, USER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> followedChannelService.getOne(CHANNEL_ID, USER_ID))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.FOLLOWED_CHANNEL_NOT_FOUND);
        }

        @Test
        @DisplayName("다른 유저의 채널 조회 시 FOLLOWED_CHANNEL_NOT_FOUND 예외를 던진다")
        void getOne_otherUserChannel_throwsNotFoundException() {
            // findByIdAndUserId returns empty when userId doesn't match — 404, not 403
            when(followedChannelRepository.findByIdAndUserId(CHANNEL_ID, OTHER_USER))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> followedChannelService.getOne(CHANNEL_ID, OTHER_USER))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.FOLLOWED_CHANNEL_NOT_FOUND);
        }
    }

    // ─── update ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("displayName 업데이트 성공")
        void update_displayName_success() {
            UpdateFollowedChannelCommand command = UpdateFollowedChannelCommand.of(
                    CHANNEL_ID, USER_ID, "New Name", null
            );
            FollowedChannel existing = activeChannel();
            FollowedChannel updated = new FollowedChannel(
                    CHANNEL_ID, USER_ID, FollowedChannelPlatform.FANPAGE,
                    CHANNEL_URL, "New Name", true, null,
                    existing.createdAt(), FollowedChannelStatus.ACTIVE
            );

            when(followedChannelRepository.findByIdAndUserId(CHANNEL_ID, USER_ID))
                    .thenReturn(Optional.of(existing));
            when(followedChannelRepository.update(existing, "New Name", null))
                    .thenReturn(updated);

            FollowedChannel result = followedChannelService.update(command);

            assertThat(result.displayName()).isEqualTo("New Name");
            verify(followedChannelRepository).update(existing, "New Name", null);
        }

        @Test
        @DisplayName("isActive false로 비활성화 성공")
        void update_deactivate_success() {
            UpdateFollowedChannelCommand command = UpdateFollowedChannelCommand.of(
                    CHANNEL_ID, USER_ID, null, false
            );
            FollowedChannel existing = activeChannel();
            FollowedChannel deactivated = new FollowedChannel(
                    CHANNEL_ID, USER_ID, FollowedChannelPlatform.FANPAGE,
                    CHANNEL_URL, DISPLAY_NAME, false, null,
                    existing.createdAt(), FollowedChannelStatus.ACTIVE
            );

            when(followedChannelRepository.findByIdAndUserId(CHANNEL_ID, USER_ID))
                    .thenReturn(Optional.of(existing));
            when(followedChannelRepository.update(existing, null, false))
                    .thenReturn(deactivated);

            FollowedChannel result = followedChannelService.update(command);

            assertThat(result.isActive()).isFalse();
        }

        @Test
        @DisplayName("채널이 없거나 본인 소유가 아니면 FOLLOWED_CHANNEL_NOT_FOUND 예외")
        void update_notFound_throwsNotFoundException() {
            UpdateFollowedChannelCommand command = UpdateFollowedChannelCommand.of(
                    CHANNEL_ID, USER_ID, "X", null
            );
            when(followedChannelRepository.findByIdAndUserId(CHANNEL_ID, USER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> followedChannelService.update(command))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.FOLLOWED_CHANNEL_NOT_FOUND);

            verify(followedChannelRepository, never()).update(any(), any(), any());
        }
    }

    // ─── delete ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("본인 채널 soft delete 성공")
        void delete_ownerRequest_callsSoftDelete() {
            FollowedChannel existing = activeChannel();

            when(followedChannelRepository.findByIdAndUserId(CHANNEL_ID, USER_ID))
                    .thenReturn(Optional.of(existing));

            followedChannelService.delete(CHANNEL_ID, USER_ID);

            verify(followedChannelRepository).softDelete(existing);
        }

        @Test
        @DisplayName("채널이 없거나 본인 소유가 아니면 FOLLOWED_CHANNEL_NOT_FOUND 예외")
        void delete_notFound_throwsNotFoundException() {
            when(followedChannelRepository.findByIdAndUserId(CHANNEL_ID, USER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> followedChannelService.delete(CHANNEL_ID, USER_ID))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.FOLLOWED_CHANNEL_NOT_FOUND);

            verify(followedChannelRepository, never()).softDelete(any());
        }

        @Test
        @DisplayName("다른 유저의 채널 삭제 시도 시 FOLLOWED_CHANNEL_NOT_FOUND 예외")
        void delete_otherUserChannel_throwsNotFoundException() {
            when(followedChannelRepository.findByIdAndUserId(CHANNEL_ID, OTHER_USER))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> followedChannelService.delete(CHANNEL_ID, OTHER_USER))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.FOLLOWED_CHANNEL_NOT_FOUND);

            verify(followedChannelRepository, never()).softDelete(any());
        }
    }
}
