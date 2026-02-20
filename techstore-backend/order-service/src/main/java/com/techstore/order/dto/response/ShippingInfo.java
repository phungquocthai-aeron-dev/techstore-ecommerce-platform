package com.techstore.order.dto.response;

import java.time.Instant;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ShippingInfo {
    private String orderCode;
    private Instant expectedDeliveryTime;
    private Double shippingFee;
}
