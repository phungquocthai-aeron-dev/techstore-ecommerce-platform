package com.techstore.warehouse.exception;

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

    // Warehouse specific errors
    WAREHOUSE_NOT_FOUND(5001, "Warehouse not found", HttpStatus.NOT_FOUND),
    WAREHOUSE_EXISTED(5002, "Warehouse with this name already exists", HttpStatus.BAD_REQUEST),
    WAREHOUSE_INACTIVE(5003, "Warehouse is inactive", HttpStatus.BAD_REQUEST),

    // Supplier specific errors
    SUPPLIER_NOT_FOUND(5101, "Supplier not found", HttpStatus.NOT_FOUND),
    SUPPLIER_EXISTED(5102, "Supplier with this phone already exists", HttpStatus.BAD_REQUEST),
    SUPPLIER_INACTIVE(5103, "Supplier is inactive", HttpStatus.BAD_REQUEST),

    // Inventory specific errors
    INVENTORY_NOT_FOUND(5201, "Inventory not found", HttpStatus.NOT_FOUND),
    INSUFFICIENT_STOCK(5202, "Insufficient stock available", HttpStatus.BAD_REQUEST),
    INVENTORY_ALREADY_EXISTS(
            5203, "Inventory for this variant and batch already exists in warehouse", HttpStatus.BAD_REQUEST),

    // Transaction specific errors
    TRANSACTION_NOT_FOUND(5301, "Transaction not found", HttpStatus.NOT_FOUND),
    INVALID_TRANSACTION_TYPE(5302, "Invalid transaction type. Must be INBOUND or OUTBOUND", HttpStatus.BAD_REQUEST),
    TRANSACTION_ALREADY_COMPLETED(5303, "Transaction already completed", HttpStatus.BAD_REQUEST),
    TRANSACTION_ALREADY_CANCELLED(5304, "Transaction already cancelled", HttpStatus.BAD_REQUEST),
    CANNOT_CANCEL_COMPLETED_TRANSACTION(5305, "Cannot cancel completed transaction", HttpStatus.BAD_REQUEST),

    // Product service errors
    VARIANT_NOT_FOUND(5401, "Variant not found in product service", HttpStatus.NOT_FOUND),
    PRODUCT_SERVICE_ERROR(5402, "Error communicating with product service", HttpStatus.SERVICE_UNAVAILABLE),

    // Staff service errors
    STAFF_NOT_FOUND(5501, "Staff not found", HttpStatus.NOT_FOUND),
    STAFF_SERVICE_ERROR(5502, "Error communicating with user service", HttpStatus.SERVICE_UNAVAILABLE),
    INVALID_STOCK_QUANTITY(5503, "Invalid stock quantity", HttpStatus.BAD_REQUEST),
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
