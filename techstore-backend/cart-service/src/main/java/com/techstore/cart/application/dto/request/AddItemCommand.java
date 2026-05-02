package com.techstore.cart.application.dto.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AddItemCommand {
    private final Long variantId;
    private final Integer quantity;
}
