package com.techstore.product.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductImageRequestDTO {
    private String url;
    private Boolean isPrimary;
}
