package com.techstore.product.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techstore.product.dto.request.VariantUpdateImageRequestDTO;
import com.techstore.product.dto.request.VariantUpdateRequestDTO;
import com.techstore.product.dto.response.ApiResponse;
import com.techstore.product.dto.response.VariantResponseDTO;
import com.techstore.product.service.VariantService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/variants")
@RequiredArgsConstructor
public class VariantController {

    private final VariantService variantService;
    private final ObjectMapper objectMapper;

    /**
     * Lấy variant theo ID
     */
    @GetMapping("/{variantId}")
    public ApiResponse<VariantResponseDTO> getById(@PathVariable Long variantId) {

        return ApiResponse.<VariantResponseDTO>builder()
                .result(variantService.getVariantById(variantId))
                .build();
    }

    /**
     * Lấy variant theo ID bao gồm stock
     */
    @GetMapping("/detail/{variantId}")
    public ApiResponse<VariantResponseDTO> getDetailById(@PathVariable Long variantId) {

        return ApiResponse.<VariantResponseDTO>builder()
                .result(variantService.getVariantWithStockById(variantId))
                .build();
    }

    @GetMapping
    public ApiResponse<List<VariantResponseDTO>> getByIds(@RequestParam List<Long> ids) {

        return ApiResponse.<List<VariantResponseDTO>>builder()
                .result(variantService.getVariantByIds(ids))
                .build();
    }

    @PostMapping("/variants/detail/batch")
    public ApiResponse<List<VariantResponseDTO>> getVariantsWithStock(@RequestBody List<Long> variantIds) {

        return ApiResponse.<List<VariantResponseDTO>>builder()
                .result(variantService.getVariantsWithStock(variantIds))
                .build();
    }

    /**
     * Cập nhật variant
     */
    @PutMapping("/{variantId}")
    public ApiResponse<VariantResponseDTO> update(
            @PathVariable Long variantId, @Valid @RequestBody VariantUpdateRequestDTO req) {

        return ApiResponse.<VariantResponseDTO>builder()
                .result(variantService.updateVariant(variantId, req))
                .build();
    }

    @PutMapping(value = "/{variantId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<VariantResponseDTO> updateVariantImage(
            @PathVariable Long variantId,
            @RequestPart(value = "file") MultipartFile file,
            @RequestPart(value = "data", required = false) String dataJson)
            throws Exception {

        VariantUpdateImageRequestDTO dto = null;

        if (dataJson != null && !dataJson.isBlank()) {
            dto = objectMapper.readValue(dataJson, VariantUpdateImageRequestDTO.class);
        }

        return ApiResponse.<VariantResponseDTO>builder()
                .result(variantService.updateVariantImage(variantId, file, dto))
                .build();
    }

    /**
     * Xóa variant (soft delete)
     */
    @DeleteMapping("/{variantId}")
    public ApiResponse<Void> delete(@PathVariable Long variantId) {

        variantService.deleteVariant(variantId);
        return ApiResponse.<Void>builder().build();
    }
}
