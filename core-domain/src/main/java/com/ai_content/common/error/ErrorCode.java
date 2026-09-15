package com.ai_content.common.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // Sample
    SAMPLE_ERROR("SAMPLE", "Sample Error Message"),

    // Common
    METHOD_ARGUMENT_TYPE_MISMATCH("COMMON-001", "Method argument type mismatch"),
    METHOD_NOT_ALLOWED("COMMON-003", "Method not allowed"),
    INTERNAL_SERVER_ERROR("COMMON-500", "Internal server error"),

    // User Auth
    USER_TOKEN_INVALID("AUTH-001", "Invalid token"),
    USER_TOKEN_EXPIRED("AUTH-002", "Token expired");

    private final String code;

    private final String message;

}