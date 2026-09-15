package controller.advice;

import com.ai_content.common.error.CustomException;
import com.ai_content.common.error.ErrorCode;
import com.ai_content.controller.advice.ErrorHttpStatusMapper;
import com.ai_content.controller.advice.ErrorResponse;
import com.ai_content.controller.advice.GlobalApiResponse;
import com.ai_content.controller.advice.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler(new ErrorHttpStatusMapper());

    @Test
    void handleCustomException_ShouldUseCustomMessage() {
        ResponseEntity<GlobalApiResponse> response  =
                globalExceptionHandler.handleCustomException(new CustomException(ErrorCode.SAMPLE_ERROR));

        HttpStatus status = HttpStatus.valueOf(response.getStatusCode().value());
        ErrorResponse data = (ErrorResponse) response.getBody().data();

        assertThat(status).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(data.errorCode()).isEqualTo(ErrorCode.SAMPLE_ERROR.getCode());
    }
}
