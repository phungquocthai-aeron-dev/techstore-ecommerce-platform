package com.techstore.warehouse.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class InventoryExportRequest {

    @NotNull
    private List<OrderItemRequest> items;

    @NotNull
    private Long orderId;
}
