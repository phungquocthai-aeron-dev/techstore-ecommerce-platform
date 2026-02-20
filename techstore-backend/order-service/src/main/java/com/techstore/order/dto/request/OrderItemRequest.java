package com.techstore.order.request;

import lombok.Data;

@Data
public class OrderItemRequest {
    Long variantId;
    Long quantity;
}
