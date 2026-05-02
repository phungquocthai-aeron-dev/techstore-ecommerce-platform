package com.techstore.cart.infrastructure.config.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {

    // Generic
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),

    // Auth
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),

    // Product / Variant
    VARIANT_NOT_FOUND(2004, "Variant not found", HttpStatus.NOT_FOUND),
    VARIANT_NOT_AVAILABLE(2005, "Variant not available", HttpStatus.BAD_REQUEST),

    // Cart
    CART_NOT_FOUND(3001, "Cart not found", HttpStatus.NOT_FOUND),
    CART_ITEM_NOT_FOUND(3002, "Cart item not found", HttpStatus.NOT_FOUND),
    CART_EMPTY(3004, "Cart is empty", HttpStatus.BAD_REQUEST),
    CHECKOUT_FAILED(3005, "Checkout failed", HttpStatus.BAD_REQUEST),
    OUT_OF_STOCK(3006, "Out of stock", HttpStatus.BAD_REQUEST),
    INVALID_QUANTITY(3007, "Invalid quantity", HttpStatus.BAD_REQUEST),

    // External services
    PRODUCT_SERVICE_UNAVAILABLE(5001, "Product service unavailable", HttpStatus.GATEWAY_TIMEOUT),
    ORDER_SERVICE_UNAVAILABLE(5002, "Order service unavailable", HttpStatus.GATEWAY_TIMEOUT);

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
