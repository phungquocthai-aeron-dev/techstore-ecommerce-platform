package com.techstore.product.dto.request;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductSearchRequestDTO {

    private String keyword;

    // multiple filter
    private List<String> brandNames;
    private List<Long> categoryIds;

    private Double minPrice;
    private Double maxPrice;

    private Integer page = 0;
    private Integer size = 10;

    private String sortBy = "id";
    private String sortDirection = "DESC";
}
