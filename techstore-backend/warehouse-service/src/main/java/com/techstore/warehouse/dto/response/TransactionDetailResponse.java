package com.techstore.warehouse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDetailResponse {
    private Long id;
    private Long quantity;
    private Long variantId;
    private InventoryResponse inventory;
}
