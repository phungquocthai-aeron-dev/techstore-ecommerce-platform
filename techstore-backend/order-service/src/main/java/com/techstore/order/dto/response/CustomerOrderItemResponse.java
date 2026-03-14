package com.techstore.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerOrderItemResponse {

    private Long orderDetailId;
    private Long variantId;

    private String name;
    private String image;

    private Integer quantity;
    private Double price;
    private Boolean reviewed;
}
