package com.techstore.warehouse.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryUpdateRequest {

    @NotNull(message = "Stock quantity is required")
    @PositiveOrZero(message = "Stock must be zero or positive")
    private Long stock;

    private String status;
    private String batchCode;
}
