package com.techstore.warehouse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostStatPoint {
    private String period;
    private Long totalCost;
    private Long totalQuantity;
    private Long transactionCount;
}
