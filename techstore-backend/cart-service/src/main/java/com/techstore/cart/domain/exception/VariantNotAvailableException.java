package com.techstore.cart.domain.exception;

public class VariantNotAvailableException extends RuntimeException {
    public VariantNotAvailableException(Long variantId) {
        super("Variant not available: " + variantId);
    }
}
