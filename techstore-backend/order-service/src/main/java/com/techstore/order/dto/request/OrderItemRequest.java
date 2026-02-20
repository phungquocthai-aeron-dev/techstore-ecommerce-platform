package com.techstore.order.dto.request;

import lombok.Data;

@Data
public class OrderItemRequest {
    Long variantId;
    Long quantity;
}
