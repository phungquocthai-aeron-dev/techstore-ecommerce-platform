package com.techstore.order.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InventoryExportRequest {

    @NotNull
    private List<OrderItemRequest> items;

    @NotNull
    private Long orderId;
}
