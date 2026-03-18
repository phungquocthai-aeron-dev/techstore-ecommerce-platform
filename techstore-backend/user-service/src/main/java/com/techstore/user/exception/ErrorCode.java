package com.techstore.user.exception;

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
    EMAIL_IS_REQUIRED(1009, "Email is required", HttpStatus.BAD_REQUEST),
    ACCOUNT_DISABLED(1010, "Account is disabled", HttpStatus.FORBIDDEN),
    INTERNAL_SERVER_ERROR(1500, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    EMAIL_NOT_VERIFIED(1401, "Email not verified by Google", HttpStatus.BAD_REQUEST),
    GOOGLE_AUTH_FAILED(1402, "Google authentication failed", HttpStatus.UNAUTHORIZED),
    ACCOUNT_ALREADY_LINKED(1403, "This Google account is already linked to another user", HttpStatus.CONFLICT),
    USER_SERVICE_UNAVAILABLE(1501, "User service is unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    ROLE_NOT_FOUND(1011, "Role not found", HttpStatus.BAD_REQUEST),
    INVALID_ROLE(1012, "Invalid role", HttpStatus.BAD_REQUEST),
    INVALID_PHONE(
            1013,
            "Số điện thoại không hợp lệ. "
                    + "Yêu cầu: 10 chữ số, bắt đầu bằng các đầu số hợp lệ tại Việt Nam (03, 05, 07, 08, 09). "
                    + "Ví dụ: 0912345678",
            HttpStatus.BAD_REQUEST),
    PASSWORD_EMPTY(1014, "Password empty", HttpStatus.BAD_REQUEST),
    PASSWORD_CONFIRM_NOT_MATCH(1015, "Password confirm not match", HttpStatus.BAD_REQUEST),
    PASSWORD_DUPLICATE(1016, "Password duplicate", HttpStatus.BAD_REQUEST),
    INVALID_OTP(1016, "Mã OTP không hợp lệ", HttpStatus.BAD_REQUEST),
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
