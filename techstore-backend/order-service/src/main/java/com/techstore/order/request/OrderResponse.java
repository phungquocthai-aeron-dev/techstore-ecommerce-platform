package com.techstore.order.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderResponse {

    private Long id;
    private Double totalPrice;
    private Double shippingFee;
    private Double vat;
    private String status;
    private String paymentStatus;
    private String paymentUrl;
}
