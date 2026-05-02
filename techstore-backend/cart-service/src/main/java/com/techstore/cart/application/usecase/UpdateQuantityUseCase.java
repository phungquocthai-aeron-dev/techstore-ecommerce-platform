package com.techstore.cart.application.usecase;

import com.techstore.cart.application.dto.request.UpdateQuantityCommand;
import com.techstore.cart.domain.exception.CartNotFoundException;
import com.techstore.cart.domain.exception.OutOfStockException;
import com.techstore.cart.domain.exception.VariantNotAvailableException;
import com.techstore.cart.domain.model.Cart;
import com.techstore.cart.domain.model.VariantInfo;
import com.techstore.cart.domain.repository.CartRepository;
import com.techstore.cart.domain.service.ProductCatalogPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * UpdateQuantityUseCase - Update or remove item. Operates on Redis only.
 * Setting quantity to 0 removes the item.
 */
@Service
@RequiredArgsConstructor
public class UpdateQuantityUseCase {

    private final CartRepository cartRepository;
    private final ProductCatalogPort productCatalogPort;

    public void execute(Long customerId, UpdateQuantityCommand command) {
        if (command.getQuantity() < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new CartNotFoundException(customerId));

        if (command.getQuantity() > 0) {
            VariantInfo variant = productCatalogPort.getVariantById(command.getVariantId());
            if (!variant.isActive()) {
                throw new VariantNotAvailableException(command.getVariantId());
            }
            if (!variant.hasStock(command.getQuantity())) {
                throw new OutOfStockException(command.getVariantId());
            }
        }

        Cart updated = cart.updateItemQuantity(command.getVariantId(), command.getQuantity());
        cartRepository.save(updated);
    }
}
