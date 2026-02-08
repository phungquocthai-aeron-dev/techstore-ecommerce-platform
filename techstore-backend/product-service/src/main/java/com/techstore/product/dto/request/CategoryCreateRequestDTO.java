package com.techstore.product.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryCreateRequestDTO {
    private String name;
    private String categoryType;
    private String pcComponentType;
    private String description;
}
