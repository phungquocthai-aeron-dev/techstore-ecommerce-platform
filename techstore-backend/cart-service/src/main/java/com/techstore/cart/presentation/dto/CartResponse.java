package com.techstore.cart.presentation.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class CartResponse {
    private Long cartId;
    private Long customerId;
    private BigDecimal totalPrice;
    private List<CartItemResponse> items;
}
