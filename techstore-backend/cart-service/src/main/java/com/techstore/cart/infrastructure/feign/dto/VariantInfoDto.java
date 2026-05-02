package com.techstore.cart.infrastructure.feign.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class VariantInfoDto {
    private Long id;
    private Long productId;
    private String color;
    private BigDecimal price;
    private Integer stock;
    private String status;
    private String imageUrl;
}
