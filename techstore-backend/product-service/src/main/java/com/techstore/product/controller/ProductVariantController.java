package com.techstore.product.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techstore.product.dto.request.VariantCreateRequestDTO;
import com.techstore.product.dto.response.ApiResponse;
import com.techstore.product.dto.response.VariantResponseDTO;
import com.techstore.product.service.VariantService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/products/{productId}/variants")
@RequiredArgsConstructor
public class ProductVariantController {

    private final VariantService variantService;

    /**
     * Thêm variant cho product
     */
    @PostMapping
    public ApiResponse<VariantResponseDTO> create(
            @PathVariable Long productId, @Valid @RequestBody VariantCreateRequestDTO req) {

        return ApiResponse.<VariantResponseDTO>builder()
                .result(variantService.createVariant(productId, req))
                .build();
    }

    /**
     * Lấy danh sách variant theo product
     */
    @GetMapping
    public ApiResponse<List<VariantResponseDTO>> getByProduct(@PathVariable Long productId) {

        return ApiResponse.<List<VariantResponseDTO>>builder()
                .result(variantService.getVariantsByProductId(productId))
                .build();
    }
}
