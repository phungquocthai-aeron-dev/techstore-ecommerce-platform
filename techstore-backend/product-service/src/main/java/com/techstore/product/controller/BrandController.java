package com.techstore.product.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.techstore.product.dto.response.BrandResponseDTO;
import com.techstore.product.dto.response.PageResponseDTO;
import com.techstore.product.service.BrandService;

@RestController
@RequestMapping("/brands")
public class BrandController {

    @Autowired
    private BrandService brandService;

    /**
     * Thêm brand mới (ADMIN only)
     */
    @PostMapping
    public ResponseEntity<BrandResponseDTO> createBrand(@RequestBody BrandCreateRequestDTO requestDTO) {
        BrandResponseDTO response = brandService.createBrand(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Cập nhật brand (ADMIN only)
     */
    @PutMapping("/{id}")
    public ResponseEntity<BrandResponseDTO> updateBrand(
            @PathVariable Long id, @RequestBody BrandUpdateRequestDTO requestDTO) {
        BrandResponseDTO response = brandService.updateBrand(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Cập nhật status brand (ADMIN only)
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<BrandResponseDTO> updateBrandStatus(
            @PathVariable Long id, @RequestBody BrandStatusUpdateRequestDTO requestDTO) {
        BrandResponseDTO response = brandService.updateBrandStatus(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Xóa brand (ADMIN only)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBrand(@PathVariable Long id) {
        brandService.deleteBrand(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lấy brand theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<BrandResponseDTO> getBrandById(@PathVariable Long id) {
        BrandResponseDTO response = brandService.getBrandById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy tất cả brands (phân trang)
     */
    @GetMapping
    public ResponseEntity<PageResponseDTO<BrandResponseDTO>> getAllBrands(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        PageResponseDTO<BrandResponseDTO> response = brandService.getAllBrands(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy brands theo status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<PageResponseDTO<BrandResponseDTO>> getBrandsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        PageResponseDTO<BrandResponseDTO> response =
                brandService.getBrandsByStatus(status, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }

    /**
     * Tìm kiếm brands
     */
    @GetMapping("/search")
    public ResponseEntity<PageResponseDTO<BrandResponseDTO>> searchBrands(
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

        PageResponseDTO<BrandResponseDTO> response = brandService.searchBrands(searchDTO);
        return ResponseEntity.ok(response);
    }
}
