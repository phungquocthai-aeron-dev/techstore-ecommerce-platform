package com.techstore.cart.application.usecase;

import com.techstore.cart.application.dto.request.AddItemCommand;
import com.techstore.cart.domain.exception.OutOfStockException;
import com.techstore.cart.domain.exception.VariantNotAvailableException;
import com.techstore.cart.domain.model.Cart;
import com.techstore.cart.domain.model.CartItem;
import com.techstore.cart.domain.model.VariantInfo;
import com.techstore.cart.domain.repository.CartRepository;
import com.techstore.cart.domain.service.ProductCatalogPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * AddItemUseCase - Adds item to cart (only writes to Redis, not MySQL directly).
 */
@Service
@RequiredArgsConstructor
public class AddItemUseCase {

    private final CartRepository cartRepository;
    private final ProductCatalogPort productCatalogPort;

    public void execute(Long customerId, AddItemCommand command) {
        if (command.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        VariantInfo variant = productCatalogPort.getVariantById(command.getVariantId());

        if (!variant.isActive()) {
            throw new VariantNotAvailableException(command.getVariantId());
        }

        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> Cart.empty(customerId));

        CartItem existing = cart.findItemByVariantId(command.getVariantId());
        int existingQty = existing != null ? existing.getQuantity() : 0;
        int totalQty = existingQty + command.getQuantity();

        if (!variant.hasStock(totalQty)) {
            throw new OutOfStockException(command.getVariantId());
        }

        Cart updated = cart.addItem(command.getVariantId(), command.getQuantity(), variant.getPrice());

        // Writes only to Redis; cron job will later sync to MySQL
        cartRepository.save(updated);
    }
}
