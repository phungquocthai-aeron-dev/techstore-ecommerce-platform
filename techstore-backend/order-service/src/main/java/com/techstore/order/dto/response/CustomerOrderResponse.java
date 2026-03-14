package com.techstore.order.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerOrderResponse {

    private Long orderId;

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
