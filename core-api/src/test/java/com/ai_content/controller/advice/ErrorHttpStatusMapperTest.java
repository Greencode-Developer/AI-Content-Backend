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
}
