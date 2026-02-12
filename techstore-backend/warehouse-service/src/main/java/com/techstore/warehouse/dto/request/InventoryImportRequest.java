package com.techstore.warehouse.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.Data;

@Data
public class InventoryImportRequest {

    @NotNull
    private Long warehouseId;

    @NotNull
    private Long variantId;

    @NotNull
    @Positive
    private Long quantity;

    private String batchCode;
}
