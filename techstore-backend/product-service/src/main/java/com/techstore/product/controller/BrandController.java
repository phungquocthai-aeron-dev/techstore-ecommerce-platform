package com.techstore.product.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.techstore.product.dto.request.BrandCreateRequestDTO;
import com.techstore.product.dto.request.BrandSearchRequestDTO;
import com.techstore.product.dto.request.BrandStatusUpdateRequestDTO;
import com.techstore.product.dto.request.BrandUpdateRequestDTO;
import com.techstore.product.dto.response.ApiResponse;
import com.techstore.product.dto.response.BrandResponseDTO;
import com.techstore.product.dto.response.PageResponseDTO;
import com.techstore.product.service.BrandService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @PostMapping
    public ApiResponse<BrandResponseDTO> createBrand(@RequestBody BrandCreateRequestDTO requestDTO) {

        return ApiResponse.<BrandResponseDTO>builder()
                .result(brandService.createBrand(requestDTO))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<BrandResponseDTO> updateBrand(
            @PathVariable Long id, @RequestBody BrandUpdateRequestDTO requestDTO) {

        return ApiResponse.<BrandResponseDTO>builder()
                .result(brandService.updateBrand(id, requestDTO))
                .build();
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<BrandResponseDTO> updateBrandStatus(
            @PathVariable Long id, @RequestBody BrandStatusUpdateRequestDTO requestDTO) {

        return ApiResponse.<BrandResponseDTO>builder()
                .result(brandService.updateBrandStatus(id, requestDTO))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteBrand(@PathVariable Long id) {

        brandService.deleteBrand(id);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/{id}")
    public ApiResponse<BrandResponseDTO> getBrandById(@PathVariable Long id) {

        return ApiResponse.<BrandResponseDTO>builder()
                .result(brandService.getBrandById(id))
                .build();
    }

    @GetMapping
    public ApiResponse<PageResponseDTO<BrandResponseDTO>> getAllBrands(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        return ApiResponse.<PageResponseDTO<BrandResponseDTO>>builder()
                .result(brandService.getAllBrands(page, size, sortBy, sortDirection))
                .build();
    }

    @GetMapping("/status/{status}")
    public ApiResponse<PageResponseDTO<BrandResponseDTO>> getBrandsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        return ApiResponse.<PageResponseDTO<BrandResponseDTO>>builder()
                .result(brandService.getBrandsByStatus(status, page, size, sortBy, sortDirection))
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<PageResponseDTO<BrandResponseDTO>> searchBrands(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        BrandSearchRequestDTO searchDTO = new BrandSearchRequestDTO();
        searchDTO.setKeyword(keyword);
        searchDTO.setStatus(status);
        searchDTO.setPage(page);
        searchDTO.setSize(size);
        searchDTO.setSortBy(sortBy);
        searchDTO.setSortDirection(sortDirection);

        return ApiResponse.<PageResponseDTO<BrandResponseDTO>>builder()
                .result(brandService.searchBrands(searchDTO))
                .build();
    }
}
