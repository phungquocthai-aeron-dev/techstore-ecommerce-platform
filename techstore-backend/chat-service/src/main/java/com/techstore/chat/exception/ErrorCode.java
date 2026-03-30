package com.techstore.chat.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),

    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),

    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),

    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),

    INVALID_EMAIL(1009, "Invalid email address", HttpStatus.BAD_REQUEST),
    EMAIL_IS_REQUIRED(1010, "Email is required", HttpStatus.BAD_REQUEST),

    ACCOUNT_DISABLED(1011, "Account is disabled", HttpStatus.FORBIDDEN),

    EMAIL_NOT_VERIFIED(1401, "Email not verified by Google", HttpStatus.BAD_REQUEST),
    GOOGLE_AUTH_FAILED(1402, "Google authentication failed", HttpStatus.UNAUTHORIZED),
    ACCOUNT_ALREADY_LINKED(1403, "This Google account is already linked to another user", HttpStatus.CONFLICT),

    USER_SERVICE_UNAVAILABLE(1501, "User service is unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    INTERNAL_SERVER_ERROR(1500, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),

    // ===== CHAT / CONVERSATION =====
    INVALID_REQUEST(2000, "Invalid request", HttpStatus.BAD_REQUEST),
    INVALID_USER_ID(2001, "Invalid user id format", HttpStatus.BAD_REQUEST),
    INVALID_USER_TYPE(2002, "Invalid user type", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(2003, "User not found", HttpStatus.NOT_FOUND),
    CONVERSATION_NOT_FOUND(2004, "Conversation not found", HttpStatus.NOT_FOUND),
    CONVERSATION_ALREADY_EXISTS(2005, "Conversation already exists", HttpStatus.CONFLICT),
    PARTICIPANT_INVALID(2006, "Participant is invalid", HttpStatus.BAD_REQUEST),
    CONVERSATION_ACCESS_DENIED(2007, "You do not have access to this conversation", HttpStatus.FORBIDDEN),

    // ===== FEIGN / INTEGRATION =====
    EXTERNAL_SERVICE_ERROR(3000, "External service error", HttpStatus.BAD_GATEWAY),
    EXTERNAL_SERVICE_TIMEOUT(3001, "External service timeout", HttpStatus.GATEWAY_TIMEOUT),
    INVALID_RESPONSE_FORMAT(3002, "Invalid response from external service", HttpStatus.BAD_GATEWAY),
    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
