package com.techstore.cart.application.usecase;

import com.techstore.cart.domain.model.Cart;
import com.techstore.cart.domain.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * GetCartUseCase - Loads cart from Redis first (via CartRepository).
 * If not present in Redis, CartRepository implementation falls back to MySQL and warm up Redis.
 */
@Service
@RequiredArgsConstructor
public class GetCartUseCase {

    private final CartRepository cartRepository;

    public Cart execute(Long customerId) {
        return cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    Cart newCart = Cart.empty(customerId);
                    return cartRepository.save(newCart);
                });
    }
}
