package com.techstore.product.dto.request;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductUpdateRequestDTO {

    private String name;
    private String description;
    private Double performanceScore;
    private Double powerConsumption;
    private String status;

    private Long brandId;
    private Long categoryId;

    private List<ProductSpecRequestDTO> specs;
}
