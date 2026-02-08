package com.techstore.cart.dto.request;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CreateOrderItemRequest {

    private Long variantId;
    private Integer quantity;
    private BigDecimal price;
}
