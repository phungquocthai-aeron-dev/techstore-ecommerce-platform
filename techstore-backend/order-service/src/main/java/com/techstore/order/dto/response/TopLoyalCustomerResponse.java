package com.techstore.order.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TopLoyalCustomerResponse {
    private Long customerId;
    private String fullName;
    private String email;
    private String phone;
    private String avatarUrl;
    private long orderCount;
    private double totalSpent;
    private double loyaltyScore;
}
