package com.techstore.warehouse.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundCostStatResponse {
    private List<CostStatPoint> data;
    private Long grandTotalCost;
    private Long grandTotalQuantity;
    private Long grandTransactionCount;
    private String periodType;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
}
