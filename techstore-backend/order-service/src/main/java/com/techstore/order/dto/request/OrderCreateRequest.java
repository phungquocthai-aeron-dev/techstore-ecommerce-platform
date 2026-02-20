package com.techstore.order.dto.request;

import java.util.List;

import lombok.Data;

@Data
public class OrderCreateRequest {

    private Long customerId;
    private Long addressId;
    private String paymentMethod;
    private Long paymentMethodId;
    private Long couponId;
    private Long shippingProviderId;

    private List<OrderItemRequest> items;
}
