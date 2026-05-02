package com.techstore.cart.application.usecase;

import com.techstore.cart.domain.exception.CartNotFoundException;
import com.techstore.cart.domain.model.Cart;
import com.techstore.cart.domain.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * RemoveItemUseCase - Removes item from cart in Redis only.
 */
@Service
@RequiredArgsConstructor
public class RemoveItemUseCase {

    private final CartRepository cartRepository;

    public void execute(Long customerId, Long variantId) {
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new CartNotFoundException(customerId));

        Cart updated = cart.removeItem(variantId);
        cartRepository.save(updated);
    }
}
