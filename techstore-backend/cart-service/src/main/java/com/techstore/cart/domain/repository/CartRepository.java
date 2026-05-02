package com.techstore.cart.domain.repository;

import com.techstore.cart.domain.model.Cart;

import java.util.Optional;

/**
 * CartRepository - Domain port for cart persistence.
 * Implementations live in the infrastructure layer (MySQL + Redis).
 */
public interface CartRepository {

    /**
     * Find cart by customer ID.
     */
    Optional<Cart> findByCustomerId(Long customerId);

    /**
     * Save or update cart.
     */
    Cart save(Cart cart);

    /**
     * Delete cart by customer ID.
     */
    void deleteByCustomerId(Long customerId);
}
