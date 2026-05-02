package com.techstore.cart.application.usecase;

import com.techstore.cart.application.dto.request.CheckoutCommand;
import com.techstore.cart.domain.exception.CartNotFoundException;
import com.techstore.cart.domain.model.Cart;
import com.techstore.cart.domain.model.CartItem;
import com.techstore.cart.domain.repository.CartRepository;
import com.techstore.cart.domain.service.OrderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * CheckoutUseCase - Places order for selected items and removes them from cart.
 * After checkout, the cart change is saved to Redis and will be synced to MySQL by cron.
 */
@Service
@RequiredArgsConstructor
public class CheckoutUseCase {

    private final CartRepository cartRepository;
    private final OrderPort orderPort;

    public void execute(CheckoutCommand command) {
        Cart cart = cartRepository.findByCustomerId(command.getCustomerId())
                .orElseThrow(() -> new CartNotFoundException(command.getCustomerId()));

        List<CartItem> itemsToCheckout = cart.getItemsByIds(command.getCartItemIds());

        if (itemsToCheckout.isEmpty()) {
            throw new com.techstore.cart.domain.exception.CartItemNotFoundException(-1L);
        }

        // Place order via Order Service
        orderPort.createOrder(command.getCustomerId(), itemsToCheckout);

        // Remove checked-out items from cart (updates Redis)
        Cart updated = cart.checkoutItems(command.getCartItemIds());
        cartRepository.save(updated);
    }
}
