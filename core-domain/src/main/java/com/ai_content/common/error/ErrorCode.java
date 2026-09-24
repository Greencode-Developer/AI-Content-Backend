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
    USER_TOKEN_EXPIRED("AUTH-002", "Token expired"),
    USER_UNAUTHORIZED("AUTH-003","Unauthorized"),
    USER_FORBIDDEN("AUTH-004","FORBIDDEN" ),
    USER_NOTFOUND("AUTH-005","NOTFOUND" ),
    USER_INVALID_PASSWORD("AUTH-006", "USER_INVALID_PASSWORD"),
    EMAIL_ALREADY_EXISTS("AUTH-007","EMAIL_ALREADY_EXISTS"),

    // Followed Channel
    FOLLOWED_CHANNEL_NOT_FOUND("CHANNEL-001", "Followed channel not found"),
    FOLLOWED_CHANNEL_DUPLICATE_URL("CHANNEL-002", "This channel URL is already being followed"),
    FOLLOWED_CHANNEL_FORBIDDEN("CHANNEL-003", "You do not have permission to access this followed channel");
    PILLAR_NOTFOUND("PILLAR-001","PILLAR_NOTFOUND"),
    InvalidTargetRatio("PILLAR-001","InvalidTargetRatio" );

    private final String code;

    private final String message;

}