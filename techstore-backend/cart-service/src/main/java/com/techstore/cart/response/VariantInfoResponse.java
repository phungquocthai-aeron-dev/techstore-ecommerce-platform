package com.techstore.cart.response;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class VariantInfoResponse {
    private Long id;
    private Long productId;
    private String color;
    private BigDecimal price;
    private Integer stock;
    private String status;
    private String imageUrl;
}
