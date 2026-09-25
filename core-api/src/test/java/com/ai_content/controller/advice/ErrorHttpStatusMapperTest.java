package com.ai_content.controller.advice;

import com.ai_content.common.error.ErrorCode;
import com.ai_content.controller.advice.ErrorHttpStatusMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import static org.assertj.core.api.Assertions.assertThat;

public class ErrorHttpStatusMapperTest {
    private final ErrorHttpStatusMapper errorHttpStatusMapper = new ErrorHttpStatusMapper();

    @Test
    void testMapErrorToHttpStatus() {
        assertThat(errorHttpStatusMapper.toHttpStatus(ErrorCode.SAMPLE_ERROR)).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldMapBrandProfileErrors() {
        assertThat(errorHttpStatusMapper.toHttpStatus(
                ErrorCode.BRAND_PROFILE_NOT_FOUND
        )).isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(errorHttpStatusMapper.toHttpStatus(
                ErrorCode.BRAND_PROFILE_VALIDATION_ERROR
        )).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldMapInvalidAndExpiredTokensToUnauthorized() {
        assertThat(errorHttpStatusMapper.toHttpStatus(
                ErrorCode.USER_TOKEN_INVALID
        )).isEqualTo(HttpStatus.UNAUTHORIZED);

        assertThat(errorHttpStatusMapper.toHttpStatus(
                ErrorCode.USER_TOKEN_EXPIRED
        )).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
