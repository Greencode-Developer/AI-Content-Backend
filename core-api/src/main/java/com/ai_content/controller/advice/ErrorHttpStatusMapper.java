package com.ai_content.controller.advice;

import com.ai_content.common.error.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ErrorHttpStatusMapper {

    public HttpStatus toHttpStatus(ErrorCode errorCode) {
        return switch (errorCode) {
            case SAMPLE_ERROR,
                    METHOD_ARGUMENT_TYPE_MISMATCH -> HttpStatus.BAD_REQUEST;
            case METHOD_NOT_ALLOWED -> HttpStatus.METHOD_NOT_ALLOWED;
            case USER_FORBIDDEN,
                    FOLLOWED_CHANNEL_FORBIDDEN -> HttpStatus.FORBIDDEN;
            case USER_NOTFOUND, USER_UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FOLLOWED_CHANNEL_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case FOLLOWED_CHANNEL_DUPLICATE_URL -> HttpStatus.CONFLICT;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}