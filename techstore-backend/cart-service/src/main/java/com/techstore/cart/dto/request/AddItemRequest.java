package com.techstore.cart.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddItemRequest {
    private Long variantId;
    private Integer quantity;
}
