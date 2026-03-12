package com.techstore.order.exception;

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
    BRAND_NOT_FOUND(2001, "Brand not found", HttpStatus.NOT_FOUND),
    CATEGORY_NOT_FOUND(2002, "Category not found", HttpStatus.NOT_FOUND),
    PRODUCT_NOT_FOUND(2003, "Product not found", HttpStatus.NOT_FOUND),
    VARIANT_NOT_FOUND(2004, "Product not found", HttpStatus.NOT_FOUND),
    VARIANT_ALREADY_EXISTS(2005, "Variant Already Exists", HttpStatus.CONFLICT),
    INVALID_PAGE_REQUEST(2006, "Invalid paging or sorting request", HttpStatus.BAD_REQUEST),
    PRODUCT_IMAGE_REQUIRED(4101, "Product must have at least one image", HttpStatus.BAD_REQUEST),
    PRODUCT_IMAGE_LIMIT_EXCEEDED(4102, "Product image limit exceeded (max 10)", HttpStatus.BAD_REQUEST),
    PRODUCT_IMAGE_NOT_FOUND(4103, "Product image not found", HttpStatus.NOT_FOUND),
    INVALID_IMAGE_PATH(4104, "Invalid image path", HttpStatus.BAD_REQUEST),
    FILE_SERVICE_BAD_REQUEST(4500, "File service bad request", HttpStatus.BAD_REQUEST),
    FILE_SERVICE_UNSUPPORTED_MEDIA(4501, "File service unsupported media type", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    FILE_SERVICE_UNAVAILABLE(4502, "File service unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    FILE_SERVICE_UNAUTHORIZED(4501, "Unauthorized when calling file service", HttpStatus.UNAUTHORIZED),

    FILE_TOO_LARGE(4502, "Uploaded file is too large", HttpStatus.PAYLOAD_TOO_LARGE),

    FILE_SERVICE_UNSUPPORTED_MEDIA_TYPE(
            4503, "Unsupported media type for file service", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    SHIPPING_PROVIDER_NOT_FOUND(4504, "Shipping provider not found", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST(4000, "Invalid request", HttpStatus.BAD_REQUEST),

    UNSUPPORTED_MEDIA_TYPE(4001, "Unsupported media type", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    INVALID_FILE_INDEX(4002, "Invalid file index", HttpStatus.BAD_REQUEST),
    INVALID_PRICE(4003, "Invalid price", HttpStatus.BAD_REQUEST),
    INVALID_COLOR(4004, "Invalid variant color", HttpStatus.BAD_REQUEST),
    VARIANT_IMAGE_NOT_FOUND(4005, "Variant image not found", HttpStatus.BAD_REQUEST),

    ADDRESS_NOT_EXISTED(3001, "Address not existed", HttpStatus.NOT_FOUND),

    COUPON_NOT_EXISTED(3002, "Coupon not existed", HttpStatus.NOT_FOUND),

    COUPON_EXISTED(3003, "Coupon already existed", HttpStatus.BAD_REQUEST),

    ORDER_NOT_FOUND(5001, "Order not found", HttpStatus.NOT_FOUND),

    ORDER_DETAIL_NOT_FOUND(5002, "Order detail not found", HttpStatus.NOT_FOUND),
    INVALID_PROVINCE(5003, "Invalid province", HttpStatus.BAD_REQUEST),
    INVALID_DISTRICT(5004, "Invalid district", HttpStatus.BAD_REQUEST),
    INVALID_WARD(5005, "Invalid ward", HttpStatus.BAD_REQUEST),

    UNSUPPORTED_SHIPPING_TYPE(6001, "Unsupported shipping type", HttpStatus.BAD_REQUEST),

    SHIPPING_SERVICE_NOT_AVAILABLE(6002, "Shipping service is not available", HttpStatus.SERVICE_UNAVAILABLE),

    INVALID_SHIPPING_TYPE(6003, "Invalid shipping type", HttpStatus.BAD_REQUEST),
    PAYMENT_METHOD_NOT_FOUND(6004, "Payment method not found", HttpStatus.BAD_REQUEST),
    INVALID_DISCOUNT_TYPE(6005, "Invalid discount type", HttpStatus.BAD_REQUEST),

    COUPON_NOT_FOUND(6006, "Coupon not found", HttpStatus.NOT_FOUND),
    COUPON_INVALID(6007, "Coupon is not active", HttpStatus.BAD_REQUEST),
    COUPON_EXPIRED(6008, "Coupon has expired or not started yet", HttpStatus.BAD_REQUEST),
    COUPON_LIMIT_REACHED(6009, "Coupon usage limit reached", HttpStatus.BAD_REQUEST),
    ORDER_NOT_ELIGIBLE_FOR_COUPON(6010, "Order does not meet coupon minimum value", HttpStatus.BAD_REQUEST),
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
