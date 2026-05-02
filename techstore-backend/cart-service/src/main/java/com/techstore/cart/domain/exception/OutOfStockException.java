package com.techstore.cart.domain.exception;

public class OutOfStockException extends RuntimeException {
    public OutOfStockException(Long variantId) {
        super("Out of stock for variant: " + variantId);
    }
}
