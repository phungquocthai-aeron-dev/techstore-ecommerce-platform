package com.techstore.warehouse.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.techstore.warehouse.dto.request.WarehouseCreateRequest;
import com.techstore.warehouse.dto.request.WarehouseUpdateRequest;
import com.techstore.warehouse.dto.response.ApiResponse;
import com.techstore.warehouse.dto.response.WarehouseResponse;
import com.techstore.warehouse.service.WarehouseService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PostMapping
    public ApiResponse<WarehouseResponse> create(@Valid @RequestBody WarehouseCreateRequest req) {
        return ApiResponse.<WarehouseResponse>builder()
                .result(warehouseService.create(req))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<WarehouseResponse> update(
            @PathVariable Long id, @Valid @RequestBody WarehouseUpdateRequest req) {
        return ApiResponse.<WarehouseResponse>builder()
                .result(warehouseService.update(id, req))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<WarehouseResponse> getById(@PathVariable Long id) {
        return ApiResponse.<WarehouseResponse>builder()
                .result(warehouseService.getById(id))
                .build();
    }

    @GetMapping("/name/{name}")
    public ApiResponse<WarehouseResponse> getByName(@PathVariable String name) {
        return ApiResponse.<WarehouseResponse>builder()
                .result(warehouseService.getByName(name))
                .build();
    }

    @GetMapping
    public ApiResponse<List<WarehouseResponse>> getAll() {
        return ApiResponse.<List<WarehouseResponse>>builder()
                .result(warehouseService.getAll())
                .build();
    }

    @GetMapping("/status/{status}")
    public ApiResponse<List<WarehouseResponse>> getByStatus(@PathVariable String status) {
        return ApiResponse.<List<WarehouseResponse>>builder()
                .result(warehouseService.getByStatus(status))
                .build();
    }

    @GetMapping("/address/{addressId}")
    public ApiResponse<List<WarehouseResponse>> getByAddress(@PathVariable String addressId) {
        return ApiResponse.<List<WarehouseResponse>>builder()
                .result(warehouseService.getByAddress(addressId))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        warehouseService.delete(id);
        return ApiResponse.<Void>builder().build();
    }

    @PutMapping("/{id}/status")
    public ApiResponse<WarehouseResponse> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ApiResponse.<WarehouseResponse>builder()
                .result(warehouseService.updateStatus(id, status))
                .build();
    }
}
