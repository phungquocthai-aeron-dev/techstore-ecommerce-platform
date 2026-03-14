package com.techstore.order.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminOrderResponse {

    private Long orderId;

    private Long customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;

    private Double totalPrice;
    private Double shippingFee;
    private Double vat;

    private String status;

    private String shippingCode;

    private LocalDateTime createdAt;
    private LocalDateTime expectedDeliveryTime;

    private String shippingProviderName;
    private String couponName;

    private String address;

    private List<CustomerOrderItemResponse> items;
}
