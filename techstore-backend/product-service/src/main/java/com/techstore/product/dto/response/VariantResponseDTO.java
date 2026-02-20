package com.techstore.product.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VariantResponseDTO {

    private Long id;
    private Long productId;
    private String color;
    private Double price;
    private Long stock;
    private String status;
    private String imageUrl;
    private Double weight;
    private String name;
}
