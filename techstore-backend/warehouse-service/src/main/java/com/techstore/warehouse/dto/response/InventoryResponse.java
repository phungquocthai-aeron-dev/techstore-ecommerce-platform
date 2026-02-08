package com.techstore.warehouse.dto.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponse {
    private Long id;
    private Long stock;
    private LocalDate updatedAt;
    private String status;
    private Long variantId;
    private String batchCode;
    private Long warehouseId;
    private String warehouseName;
    private VariantInfo variantInfo;
}
