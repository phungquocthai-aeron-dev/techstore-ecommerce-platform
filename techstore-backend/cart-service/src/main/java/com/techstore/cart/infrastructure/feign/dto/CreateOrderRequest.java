package com.techstore.cart.infrastructure.feign.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateOrderRequest {
    private Long customerId;
    private BigDecimal totalAmount;
    private List<CreateOrderItemRequest> items;

    @Data
    public static class CreateOrderItemRequest {
        private Long variantId;
        private Integer quantity;
        private BigDecimal price;
    }
}
