package com.techstore.cart.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartResponse {

    private Long cartId;
    private Long customerId;
    private BigDecimal totalPrice;
    private List<CartItemResponse> items;
}
