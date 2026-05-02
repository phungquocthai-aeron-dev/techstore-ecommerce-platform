package com.techstore.cart.infrastructure.config.exception;

import com.techstore.cart.domain.exception.CartItemNotFoundException;
import com.techstore.cart.domain.exception.CartNotFoundException;
import com.techstore.cart.domain.exception.OutOfStockException;
import com.techstore.cart.domain.exception.VariantNotAvailableException;
import com.techstore.cart.infrastructure.feign.exception.OrderServiceException;
import com.techstore.cart.infrastructure.feign.exception.ProductServiceException;
import com.techstore.cart.presentation.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * GlobalExceptionHandler - Translates all exceptions to ApiResponse.
 *
 * Mapping strategy:
 *   - Domain exceptions  → meaningful HTTP errors (no leaking infra details)
 *   - AppException       → uses its own ErrorCode
 *   - Infrastructure ex. → mapped to 5xx gateway errors
 *   - RuntimeException   → 500 fallback
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // ─── Domain exceptions ──────────────────────────────────────────────────

    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleCartNotFound(CartNotFoundException ex) {
        return error(ErrorCode.CART_NOT_FOUND);
    }

    @ExceptionHandler(CartItemNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleCartItemNotFound(CartItemNotFoundException ex) {
        return error(ErrorCode.CART_ITEM_NOT_FOUND);
    }

    @ExceptionHandler(OutOfStockException.class)
    public ResponseEntity<ApiResponse<Void>> handleOutOfStock(OutOfStockException ex) {
        return error(ErrorCode.OUT_OF_STOCK);
    }

    @ExceptionHandler(VariantNotAvailableException.class)
    public ResponseEntity<ApiResponse<Void>> handleVariantNotAvailable(VariantNotAvailableException ex) {
        return error(ErrorCode.VARIANT_NOT_AVAILABLE);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return error(ErrorCode.INVALID_QUANTITY);
    }

    // ─── Application / infrastructure exceptions ────────────────────────────

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        return error(ex.getErrorCode());
    }

    @ExceptionHandler(ProductServiceException.class)
    public ResponseEntity<ApiResponse<Void>> handleProductServiceEx(ProductServiceException ex) {
        log.error("Product service error: {}", ex.getMessage(), ex);
        return error(ErrorCode.PRODUCT_SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(OrderServiceException.class)
    public ResponseEntity<ApiResponse<Void>> handleOrderServiceEx(OrderServiceException ex) {
        log.error("Order service error: {}", ex.getMessage(), ex);
        return error(ErrorCode.ORDER_SERVICE_UNAVAILABLE);
    }

    // ─── Security exceptions ─────────────────────────────────────────────────

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return error(ErrorCode.UNAUTHORIZED);
    }

    // ─── Fallback ─────────────────────────────────────────────────────────────

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntime(RuntimeException ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return error(ErrorCode.UNCATEGORIZED_EXCEPTION);
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private ResponseEntity<ApiResponse<Void>> error(ErrorCode code) {
        return ResponseEntity.status(code.getStatusCode())
                .body(ApiResponse.<Void>builder()
                        .code(code.getCode())
                        .message(code.getMessage())
                        .build());
    }
}
