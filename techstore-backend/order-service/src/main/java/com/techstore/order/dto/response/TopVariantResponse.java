package com.techstore.order.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TopVariantResponse {
    private Long variantId;
    private String name;
    private String imageUrl;
    private long totalQuantitySold;
    private double totalRevenue;
}
