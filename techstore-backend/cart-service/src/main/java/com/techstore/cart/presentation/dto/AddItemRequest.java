package com.techstore.cart.presentation.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddItemRequest {
    private Long variantId;
    private Integer quantity;
}
