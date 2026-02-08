package com.techstore.product.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductListResponseDTO {

    private Long id;
    private String name;
    private Double basePrice;
    private String status;
    private String brandName;
    private String categoryName;

    private String primaryImage;
}
