package com.techstore.product.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VariantUpdateRequestDTO {

    private String color;
    private Double price;
    private String status;
}
