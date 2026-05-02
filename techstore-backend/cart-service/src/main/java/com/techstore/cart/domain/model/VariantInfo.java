package com.techstore.cart.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * VariantInfo - Domain value object representing product variant data fetched from Product Service.
 */
@Getter
@Builder
public class VariantInfo {
    private final Long id;
    private final Long productId;
    private final String color;
    private final BigDecimal price;
    private final Integer stock;
    private final String status;
    private final String imageUrl;

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    public boolean hasStock(int requestedQuantity) {
        return stock != null && stock >= requestedQuantity;
    }
}
