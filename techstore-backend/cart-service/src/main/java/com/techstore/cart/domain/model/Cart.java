package com.techstore.cart.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Cart Aggregate Root - Pure domain model, no framework dependencies.
 */
@Getter
@Builder
@With
public class Cart {

    private final Long id;
    private final Long customerId;
    private final List<CartItem> items;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static Cart empty(Long customerId) {
        return Cart.builder()
                .customerId(customerId)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public Cart addItem(Long variantId, int quantity, BigDecimal price) {
        List<CartItem> newItems = new ArrayList<>(this.items);
        CartItem existing = findItemByVariantId(variantId);

        if (existing != null) {
            newItems.remove(existing);
            newItems.add(existing.withQuantity(existing.getQuantity() + quantity));
        } else {
            newItems.add(CartItem.of(variantId, quantity, price));
        }

        return this.withItems(newItems).withUpdatedAt(LocalDateTime.now());
    }

    public Cart updateItemQuantity(Long variantId, int quantity) {
        List<CartItem> newItems = new ArrayList<>(this.items);
        CartItem existing = findItemByVariantId(variantId);

        if (existing == null) {
            throw new com.techstore.cart.domain.exception.CartItemNotFoundException(variantId);
        }

        newItems.remove(existing);
        if (quantity > 0) {
            newItems.add(existing.withQuantity(quantity));
        }

        return this.withItems(newItems).withUpdatedAt(LocalDateTime.now());
    }

    public Cart removeItem(Long variantId) {
        CartItem existing = findItemByVariantId(variantId);
        if (existing == null) {
            throw new com.techstore.cart.domain.exception.CartItemNotFoundException(variantId);
        }
        List<CartItem> newItems = new ArrayList<>(this.items);
        newItems.remove(existing);
        return this.withItems(newItems).withUpdatedAt(LocalDateTime.now());
    }

    public Cart checkoutItems(List<Long> cartItemIds) {
        List<CartItem> newItems = new ArrayList<>(this.items);
        newItems.removeIf(item -> cartItemIds.contains(item.getId()));
        return this.withItems(newItems).withUpdatedAt(LocalDateTime.now());
    }

    public List<CartItem> getItemsByIds(List<Long> ids) {
        return this.items.stream()
                .filter(item -> ids.contains(item.getId()))
                .toList();
    }

    public CartItem findItemByVariantId(Long variantId) {
        return this.items.stream()
                .filter(i -> i.getVariantId().equals(variantId))
                .findFirst()
                .orElse(null);
    }

    public BigDecimal getTotalPrice() {
        return items.stream()
                .map(CartItem::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items != null ? items : new ArrayList<>());
    }

    private Cart withUpdatedAt(LocalDateTime updatedAt) {
        return Cart.builder()
                .id(this.id)
                .customerId(this.customerId)
                .items(this.items)
                .createdAt(this.createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
