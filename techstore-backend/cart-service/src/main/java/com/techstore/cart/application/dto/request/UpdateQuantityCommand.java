package com.techstore.cart.application.dto.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UpdateQuantityCommand {
    private final Long variantId;
    private final Integer quantity;
}
