package com.techstore.cart.exception;

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
    VARIANT_NOT_FOUND(2004, "Variant not found", HttpStatus.NOT_FOUND),
    VARIANT_ALREADY_EXISTS(2005, "Variant Already Exists", HttpStatus.CONFLICT),
    INVALID_PAGE_REQUEST(2006, "Invalid paging or sorting request", HttpStatus.BAD_REQUEST),
    CART_NOT_FOUND(3001, "Cart not found", HttpStatus.NOT_FOUND),
    CART_ITEM_NOT_FOUND(3002, "Cart item not found", HttpStatus.NOT_FOUND),
    CART_ITEM_INVALID_QUANTITY(3003, "Invalid cart item quantity", HttpStatus.BAD_REQUEST),
    CART_EMPTY(3004, "Cart is empty", HttpStatus.BAD_REQUEST),
    CHECKOUT_FAILED(3005, "Checkout failed", HttpStatus.BAD_REQUEST),
    OUT_OF_STOCK(3006, "Out of stock", HttpStatus.BAD_REQUEST),
    INVALID_QUANTITY(3007, "Invalid quantity", HttpStatus.BAD_REQUEST),
    PRODUCT_SERVICE_TIMEOUT(5001, "Product service timeout", HttpStatus.GATEWAY_TIMEOUT),
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
