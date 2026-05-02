package com.techstore.cart.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

/**
 * CartItem - Value Object inside the Cart aggregate.
 */
@Getter
@Builder
@With
public class CartItem {

    private static final AtomicLong TEMP_ID_COUNTER = new AtomicLong(-1);

    private final Long id;
    private final Long variantId;
    private final Integer quantity;
    private final BigDecimal priceSnapshot;
    private final LocalDateTime addedAt;

    public static CartItem of(Long variantId, int quantity, BigDecimal price) {
        return CartItem.builder()
                .id(TEMP_ID_COUNTER.getAndDecrement()) // temporary negative ID until persisted
                .variantId(variantId)
                .quantity(quantity)
                .priceSnapshot(price)
                .addedAt(LocalDateTime.now())
                .build();
    }

    public BigDecimal getSubTotal() {
        if (priceSnapshot == null || quantity == null) return BigDecimal.ZERO;
        return priceSnapshot.multiply(BigDecimal.valueOf(quantity));
    }
}
