package com.techstore.warehouse.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseTransactionResponse {
    private Long id;
    private String note;
    private String transactionType;
    private String referenceType;
    private String orderId;
    private Long staffId;
    private String status;
    private LocalDateTime createdAt;
    private SupplierResponse supplier;
    private WarehouseResponse warehouse;
    private List<TransactionDetailResponse> details;
}
