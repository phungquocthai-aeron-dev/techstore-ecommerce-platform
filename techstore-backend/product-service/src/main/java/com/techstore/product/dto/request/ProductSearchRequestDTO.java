package com.techstore.product.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductSearchRequestDTO {
    private String keyword;
    private String brandName;
    private Double minPrice;
    private Double maxPrice;
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "id";
    private String sortDirection = "DESC";
}
