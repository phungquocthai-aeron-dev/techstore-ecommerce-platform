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

import com.techstore.warehouse.dto.request.SupplierCreateRequest;
import com.techstore.warehouse.dto.request.SupplierUpdateRequest;
import com.techstore.warehouse.dto.response.ApiResponse;
import com.techstore.warehouse.dto.response.SupplierResponse;
import com.techstore.warehouse.service.SupplierService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    public ApiResponse<SupplierResponse> create(@Valid @RequestBody SupplierCreateRequest req) {
        return ApiResponse.<SupplierResponse>builder()
                .result(supplierService.create(req))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<SupplierResponse> update(@PathVariable Long id, @Valid @RequestBody SupplierUpdateRequest req) {
        return ApiResponse.<SupplierResponse>builder()
                .result(supplierService.update(id, req))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<SupplierResponse> getById(@PathVariable Long id) {
        return ApiResponse.<SupplierResponse>builder()
                .result(supplierService.getById(id))
                .build();
    }

    @GetMapping("/phone/{phone}")
    public ApiResponse<SupplierResponse> getByPhone(@PathVariable String phone) {
        return ApiResponse.<SupplierResponse>builder()
                .result(supplierService.getByPhone(phone))
                .build();
    }

    @GetMapping
    public ApiResponse<List<SupplierResponse>> getAll() {
        return ApiResponse.<List<SupplierResponse>>builder()
                .result(supplierService.getAll())
                .build();
    }

    @GetMapping("/status/{status}")
    public ApiResponse<List<SupplierResponse>> getByStatus(@PathVariable String status) {
        return ApiResponse.<List<SupplierResponse>>builder()
                .result(supplierService.getByStatus(status))
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<List<SupplierResponse>> searchByName(@RequestParam String name) {
        return ApiResponse.<List<SupplierResponse>>builder()
                .result(supplierService.searchByName(name))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        supplierService.delete(id);
        return ApiResponse.<Void>builder().build();
    }

    @PutMapping("/{id}/status")
    public ApiResponse<SupplierResponse> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ApiResponse.<SupplierResponse>builder()
                .result(supplierService.updateStatus(id, status))
                .build();
    }
}
