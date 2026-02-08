package com.techstore.cart.response;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemResponse {

    private Long id;
    private Long variantId;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subTotal;
}
