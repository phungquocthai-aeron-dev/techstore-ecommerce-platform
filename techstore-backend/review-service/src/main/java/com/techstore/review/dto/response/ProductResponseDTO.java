package com.techstore.review.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductResponseDTO {

    private Long id;
    private String name;
    private String description;

    private Double basePrice;

    private Double performanceScore;
    private Double powerConsumption;
    private String status;
}
