package com.ai_content.controller.advice;

import com.ai_content.common.error.ErrorCode;
import com.ai_content.controller.advice.ErrorHttpStatusMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

public class ErrorHttpStatusMapperTest {

    private final ErrorHttpStatusMapper mapper = new ErrorHttpStatusMapper();

    @Test
    void testMapErrorToHttpStatus() {
        assertThat(mapper.toHttpStatus(ErrorCode.SAMPLE_ERROR)).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ─── Followed Channel ────────────────────────────────────────────────────

    @Test
    @DisplayName("FOLLOWED_CHANNEL_NOT_FOUND → 404 Not Found")
    void followedChannelNotFound_returns404() {
        assertThat(mapper.toHttpStatus(ErrorCode.FOLLOWED_CHANNEL_NOT_FOUND))
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("FOLLOWED_CHANNEL_DUPLICATE_URL → 409 Conflict (중복 follow는 500이 아님)")
    void followedChannelDuplicateUrl_returns409() {
        assertThat(mapper.toHttpStatus(ErrorCode.FOLLOWED_CHANNEL_DUPLICATE_URL))
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("FOLLOWED_CHANNEL_FORBIDDEN → 403 Forbidden")
    void followedChannelForbidden_returns403() {
        assertThat(mapper.toHttpStatus(ErrorCode.FOLLOWED_CHANNEL_FORBIDDEN))
                .isEqualTo(HttpStatus.FORBIDDEN);
    }
}
