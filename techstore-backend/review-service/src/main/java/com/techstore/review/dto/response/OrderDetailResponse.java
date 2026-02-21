package com.techstore.review.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderDetailResponse {
    private Long id;

    private Integer quantity;
    private Double price;
    private Long variantId;
    private String status;
    private Double totalWeight;
    private String name;
}
