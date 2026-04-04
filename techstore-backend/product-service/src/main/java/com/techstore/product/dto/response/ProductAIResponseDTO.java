package com.techstore.product.dto.response;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductAIResponseDTO {

    private Long id;
    private String name;
    private Double basePrice;

    private String categoryType;
    private String pcComponentType;
    private Double performanceScore;
    private Double powerConsumption;

    private String primaryImage;

    private List<ProductSpecDTO> specs;
}
