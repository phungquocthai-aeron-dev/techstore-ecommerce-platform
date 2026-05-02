package com.techstore.cart.application.dto.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class CheckoutCommand {
    private final Long customerId;
    private final List<Long> cartItemIds;
}
