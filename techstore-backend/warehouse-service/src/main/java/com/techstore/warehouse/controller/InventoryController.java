package com.techstore.warehouse.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.techstore.warehouse.dto.request.InventoryUpdateRequest;
import com.techstore.warehouse.dto.response.ApiResponse;
import com.techstore.warehouse.dto.response.InventoryResponse;
import com.techstore.warehouse.dto.response.VariantStockResponse;
import com.techstore.warehouse.service.InventoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/available")
    public ApiResponse<List<InventoryResponse>> findAvailableInventory(
            @RequestParam Long warehouseId, @RequestParam Long variantId, @RequestParam Long requiredQuantity) {

        return ApiResponse.<List<InventoryResponse>>builder()
                .result(inventoryService.findActiveInventories(warehouseId, variantId))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<InventoryResponse> update(
            @PathVariable Long id, @Valid @RequestBody InventoryUpdateRequest req) {
        return ApiResponse.<InventoryResponse>builder()
                .result(inventoryService.update(id, req))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<InventoryResponse> getById(@PathVariable Long id) {
        return ApiResponse.<InventoryResponse>builder()
                .result(inventoryService.getById(id))
                .build();
    }

    @GetMapping("/warehouse/{warehouseId}")
    public ApiResponse<List<InventoryResponse>> getByWarehouse(@PathVariable Long warehouseId) {
        return ApiResponse.<List<InventoryResponse>>builder()
                .result(inventoryService.getByWarehouse(warehouseId))
                .build();
    }

    @GetMapping("/variant/{variantId}")
    public ApiResponse<List<InventoryResponse>> getByVariant(@PathVariable Long variantId) {
        return ApiResponse.<List<InventoryResponse>>builder()
                .result(inventoryService.getByVariant(variantId))
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<List<InventoryResponse>> getByWarehouseAndVariant(
            @RequestParam Long warehouseId, @RequestParam Long variantId) {
        return ApiResponse.<List<InventoryResponse>>builder()
                .result(inventoryService.getByWarehouseAndVariant(warehouseId, variantId))
                .build();
    }

    @GetMapping
    public ApiResponse<List<InventoryResponse>> getAll() {
        return ApiResponse.<List<InventoryResponse>>builder()
                .result(inventoryService.getAll())
                .build();
    }

    @GetMapping("/status/{status}")
    public ApiResponse<List<InventoryResponse>> getByStatus(@PathVariable String status) {
        return ApiResponse.<List<InventoryResponse>>builder()
                .result(inventoryService.getByStatus(status))
                .build();
    }

    @GetMapping("/variant/{variantId}/total-stock")
    public ApiResponse<Long> getTotalStockByVariant(@PathVariable Long variantId) {
        return ApiResponse.<Long>builder()
                .result(inventoryService.getTotalStockByVariant(variantId))
                .build();
    }

    @PostMapping("/variant/total-stock/batch")
    public ApiResponse<List<VariantStockResponse>> getTotalStockByVariants(@RequestBody List<Long> variantIds) {

        return ApiResponse.<List<VariantStockResponse>>builder()
                .result(inventoryService.getTotalStockByVariantIds(variantIds))
                .build();
    }
}
