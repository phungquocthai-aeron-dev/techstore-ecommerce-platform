package com.techstore.chatbot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariantInfoWithStock {
    private Long id;
    private Long productId;
    private String color;
    private Double price;
    private Long stock;
    private String status;
    private String imageUrl;
    private Double weight;
    private String name;
}
