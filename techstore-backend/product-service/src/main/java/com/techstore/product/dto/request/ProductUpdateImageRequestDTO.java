package com.techstore.product.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductUpdateImageRequestDTO {
    private String oldUrl;
    private Boolean isPrimary;
    private Integer fileIndex;
}
