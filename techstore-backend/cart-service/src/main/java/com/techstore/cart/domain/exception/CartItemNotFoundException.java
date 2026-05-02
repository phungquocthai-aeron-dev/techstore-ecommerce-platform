package com.techstore.cart.domain.exception;

public class CartItemNotFoundException extends RuntimeException {
    public CartItemNotFoundException(Long variantId) {
        super("Cart item not found for variant: " + variantId);
    }
}
