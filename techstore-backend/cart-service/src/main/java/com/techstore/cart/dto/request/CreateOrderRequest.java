package com.techstore.cart.dto.request;

import java.math.BigDecimal;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrderRequest {
    private Long customerId;
    private BigDecimal totalAmount;
    private List<CreateOrderItemRequest> items;
}
