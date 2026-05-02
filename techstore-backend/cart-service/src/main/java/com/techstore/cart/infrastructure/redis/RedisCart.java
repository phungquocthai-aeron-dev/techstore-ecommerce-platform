package com.techstore.cart.infrastructure.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * RedisCart - Serializable data model stored in Redis as JSON.
 * Key pattern: cart:user:{customerId}
 * TTL: 7 days (reset on each write)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedisCart implements Serializable {

    private Long customerId;
    private List<RedisCartItem> items = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RedisCartItem implements Serializable {
        private Long id;
        private Long variantId;
        private Integer quantity;
        private BigDecimal priceSnapshot;
        private LocalDateTime addedAt;
    }
}
