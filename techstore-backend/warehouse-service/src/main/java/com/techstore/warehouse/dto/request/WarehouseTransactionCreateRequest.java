package com.techstore.warehouse.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseTransactionCreateRequest {

    private String note;

    @NotBlank(message = "Transaction type is required (INBOUND/OUTBOUND)")
    private String transactionType;

    @NotBlank(message = "Reference type is required")
    private String referenceType;

    private String orderId;

    @NotNull(message = "Staff ID is required")
    private Long staffId;

    private Long supplierId;

    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;

    @NotEmpty(message = "Transaction details are required")
    private List<TransactionDetailRequest> details;
}
