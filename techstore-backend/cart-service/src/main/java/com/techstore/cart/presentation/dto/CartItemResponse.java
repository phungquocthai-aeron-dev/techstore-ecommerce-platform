package com.techstore.cart.presentation.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CartItemResponse {
    private Long id;
    private Long variantId;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subTotal;
}
