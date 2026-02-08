package com.techstore.warehouse.controller;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.techstore.warehouse.dto.request.WarehouseTransactionCreateRequest;
import com.techstore.warehouse.dto.response.ApiResponse;
import com.techstore.warehouse.dto.response.WarehouseTransactionResponse;
import com.techstore.warehouse.service.WarehouseTransactionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class WarehouseTransactionController {

    private final WarehouseTransactionService transactionService;

    /**
     * Tạo phiếu nhập hàng (INBOUND)
     */
    @PostMapping("/inbound")
    public ApiResponse<WarehouseTransactionResponse> createInbound(
            @Valid @RequestBody WarehouseTransactionCreateRequest req) {
        return ApiResponse.<WarehouseTransactionResponse>builder()
                .result(transactionService.createInboundTransaction(req))
                .build();
    }

    /**
     * Tạo phiếu xuất hàng (OUTBOUND)
     */
    @PostMapping("/outbound")
    public ApiResponse<WarehouseTransactionResponse> createOutbound(
            @Valid @RequestBody WarehouseTransactionCreateRequest req) {
        return ApiResponse.<WarehouseTransactionResponse>builder()
                .result(transactionService.createOutboundTransaction(req))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<WarehouseTransactionResponse> getById(@PathVariable Long id) {
        return ApiResponse.<WarehouseTransactionResponse>builder()
                .result(transactionService.getById(id))
                .build();
    }

    @GetMapping
    public ApiResponse<List<WarehouseTransactionResponse>> getAll() {
        return ApiResponse.<List<WarehouseTransactionResponse>>builder()
                .result(transactionService.getAll())
                .build();
    }

    @GetMapping("/warehouse/{warehouseId}")
    public ApiResponse<List<WarehouseTransactionResponse>> getByWarehouse(@PathVariable Long warehouseId) {
        return ApiResponse.<List<WarehouseTransactionResponse>>builder()
                .result(transactionService.getByWarehouse(warehouseId))
                .build();
    }

    @GetMapping("/supplier/{supplierId}")
    public ApiResponse<List<WarehouseTransactionResponse>> getBySupplier(@PathVariable Long supplierId) {
        return ApiResponse.<List<WarehouseTransactionResponse>>builder()
                .result(transactionService.getBySupplier(supplierId))
                .build();
    }

    @GetMapping("/type/{type}")
    public ApiResponse<List<WarehouseTransactionResponse>> getByType(@PathVariable String type) {
        return ApiResponse.<List<WarehouseTransactionResponse>>builder()
                .result(transactionService.getByType(type))
                .build();
    }

    @GetMapping("/status/{status}")
    public ApiResponse<List<WarehouseTransactionResponse>> getByStatus(@PathVariable String status) {
        return ApiResponse.<List<WarehouseTransactionResponse>>builder()
                .result(transactionService.getByStatus(status))
                .build();
    }

    @GetMapping("/order/{orderId}")
    public ApiResponse<List<WarehouseTransactionResponse>> getByOrderId(@PathVariable String orderId) {
        return ApiResponse.<List<WarehouseTransactionResponse>>builder()
                .result(transactionService.getByOrderId(orderId))
                .build();
    }

    @GetMapping("/date-range")
    public ApiResponse<List<WarehouseTransactionResponse>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ApiResponse.<List<WarehouseTransactionResponse>>builder()
                .result(transactionService.getByDateRange(startDate, endDate))
                .build();
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<WarehouseTransactionResponse> cancel(@PathVariable Long id) {
        return ApiResponse.<WarehouseTransactionResponse>builder()
                .result(transactionService.cancelTransaction(id))
                .build();
    }
}
