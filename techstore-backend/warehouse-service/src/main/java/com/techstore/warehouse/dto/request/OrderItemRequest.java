package com.techstore.warehouse.dto.request;

import lombok.Data;

@Data
public class OrderItemRequest {
    Long variantId;
    Long quantity;
}
