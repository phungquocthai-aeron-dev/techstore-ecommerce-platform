package com.techstore.quizgame.exception;

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
            4001,
            "Số điện thoại không hợp lệ. "
                    + "Yêu cầu: 10 chữ số, bắt đầu bằng các đầu số hợp lệ tại Việt Nam (03, 05, 07, 08, 09). "
                    + "Ví dụ: 0912345678",
            HttpStatus.BAD_REQUEST),
    INVALID_WEIGHT(1013, "Invalid weight", HttpStatus.BAD_REQUEST),
    BRAND_NOT_FOUND(2001, "Brand not found", HttpStatus.NOT_FOUND),
    CATEGORY_NOT_FOUND(2002, "Category not found", HttpStatus.NOT_FOUND),
    PRODUCT_NOT_FOUND(2003, "Product not found", HttpStatus.NOT_FOUND),
    VARIANT_NOT_FOUND(2004, "Product not found", HttpStatus.NOT_FOUND),
    VARIANT_ALREADY_EXISTS(2005, "Variant Already Exists", HttpStatus.CONFLICT),
    INVALID_PAGE_REQUEST(2006, "Invalid paging or sorting request", HttpStatus.BAD_REQUEST),

    // Game errors
    DAILY_PLAY_LIMIT_EXCEEDED(4001, "Bạn đã hết lượt chơi trong ngày", HttpStatus.BAD_REQUEST),
    GAME_SESSION_NOT_FOUND(4002, "Không tìm thấy game session", HttpStatus.NOT_FOUND),
    NOT_ENOUGH_QUESTIONS(4003, "Không đủ câu hỏi trong hệ thống", HttpStatus.INTERNAL_SERVER_ERROR),

    // Coupon errors
    COUPON_NOT_FOUND(4010, "Không tìm thấy coupon", HttpStatus.NOT_FOUND),
    COUPON_INACTIVE(4011, "Coupon không còn hoạt động", HttpStatus.BAD_REQUEST),
    COUPON_OUT_OF_STOCK(4012, "Coupon đã hết số lượng", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_POINTS(4013, "Điểm không đủ để đổi coupon", HttpStatus.BAD_REQUEST),
    COUPON_VALIDATION_FAILED(4014, "Coupon không hợp lệ từ order-service", HttpStatus.BAD_REQUEST),
    COUPON_EXPIRED(4015, "Coupon đã hết hạn sử dụng", HttpStatus.BAD_REQUEST),
    COUPON_ASSIGN_FAILED(4016, "Không thể gán coupon, vui lòng thử lại", HttpStatus.INTERNAL_SERVER_ERROR),
    COUPON_ALREADY_DELETED(4016, "Mã giảm giá đã bị xóa trước đó", HttpStatus.BAD_REQUEST),
    COUPON_ALREADY_EXISTS(4016, "Mã giảm giá đã tồn tại", HttpStatus.BAD_REQUEST),

    // User errors
    USER_NOT_FOUND(4020, "Không tìm thấy user", HttpStatus.NOT_FOUND),

    // System errors
    INTERNAL_ERROR(5000, "Lỗi hệ thống", HttpStatus.INTERNAL_SERVER_ERROR),

    INVALID_REQUEST(4000, "Invalid request", HttpStatus.BAD_REQUEST),

    UNSUPPORTED_MEDIA_TYPE(4001, "Unsupported media type", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    INVALID_FILE_INDEX(4002, "Invalid file index", HttpStatus.BAD_REQUEST),
    INVALID_PRICE(4003, "Invalid price", HttpStatus.BAD_REQUEST),
    INVALID_COLOR(4004, "Invalid variant color", HttpStatus.BAD_REQUEST),
    VARIANT_IMAGE_NOT_FOUND(4005, "Variant image not found", HttpStatus.BAD_REQUEST),

    TOPIC_NOT_FOUND(404, "Chủ đề không tồn tại", HttpStatus.BAD_REQUEST),
    TOPIC_NAME_EXISTED(409, "Tên chủ đề đã tồn tại", HttpStatus.BAD_REQUEST),
    QUESTION_NOT_FOUND(404, "Câu hỏi không tồn tại", HttpStatus.BAD_REQUEST),
    ANSWER_NOT_FOUND(404, "Đáp án không tồn tại", HttpStatus.BAD_REQUEST),
    QUESTION_MUST_HAVE_ONE_CORRECT_ANSWER(400, "Câu hỏi phải có đúng 1 đáp án đúng", HttpStatus.BAD_REQUEST),
    ANSWER_NOT_BELONG_TO_QUESTION(400, "Đáp án không thuộc câu hỏi này", HttpStatus.BAD_REQUEST),
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
