package com.techstore.product.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techstore.product.dto.request.ProductCreateRequestDTO;
import com.techstore.product.dto.request.ProductSearchRequestDTO;
import com.techstore.product.dto.request.ProductStatusUpdateRequestDTO;
import com.techstore.product.dto.request.ProductUpdateImageRequestDTO;
import com.techstore.product.dto.request.ProductUpdateRequestDTO;
import com.techstore.product.dto.response.ApiResponse;
import com.techstore.product.dto.response.PageResponseDTO;
import com.techstore.product.dto.response.ProductListResponseDTO;
import com.techstore.product.dto.response.ProductResponseDTO;
import com.techstore.product.service.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ObjectMapper objectMapper;

    /**
     * Thêm sản phẩm mới (ADMIN)
     */
    @PostMapping
    public ApiResponse<ProductResponseDTO> create(@Valid @RequestBody ProductCreateRequestDTO req) {

        return ApiResponse.<ProductResponseDTO>builder()
                .result(productService.createProduct(req))
                .build();
    }

    /**
     * Cập nhật thông tin sản phẩm (ADMIN)
     */
    @PutMapping("/{id}")
    public ApiResponse<ProductResponseDTO> updateInfo(
            @PathVariable Long id, @Valid @RequestBody ProductUpdateRequestDTO req) {
        return ApiResponse.<ProductResponseDTO>builder()
                .result(productService.updateProductInfo(id, req))
                .build();
    }

    /**
     * Cập nhật ảnh sản phẩm (ADMIN)
     */
    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ProductResponseDTO> updateImages(
            @PathVariable Long id,
            @RequestPart("files") MultipartFile[] files,
            @RequestPart(value = "images", required = false) String imagesJson)
            throws Exception {

        List<ProductUpdateImageRequestDTO> imageDTOs = null;

        if (imagesJson != null && !imagesJson.isBlank()) {
            imageDTOs = objectMapper.readValue(imagesJson, new TypeReference<List<ProductUpdateImageRequestDTO>>() {});
        }

        return ApiResponse.<ProductResponseDTO>builder()
                .result(productService.updateProductImages(id, files, imageDTOs))
                .build();
    }

    /**
     * Cập nhật trạng thái sản phẩm (ADMIN)
     */
    @PatchMapping("/{id}/status")
    public ApiResponse<ProductResponseDTO> updateStatus(
            @PathVariable Long id, @RequestBody ProductStatusUpdateRequestDTO req) {

        return ApiResponse.<ProductResponseDTO>builder()
                .result(productService.updateProductStatus(id, req))
                .build();
    }

    /**
     * Lấy sản phẩm theo ID
     */
    @GetMapping("/{id}")
    public ApiResponse<ProductResponseDTO> findById(@PathVariable Long id) {

        return ApiResponse.<ProductResponseDTO>builder()
                .result(productService.getProductById(id))
                .build();
    }

    /**
     * Lấy danh sách sản phẩm (phân trang)
     */
    @GetMapping
    public ApiResponse<PageResponseDTO<ProductListResponseDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        return ApiResponse.<PageResponseDTO<ProductListResponseDTO>>builder()
                .result(productService.getAllProducts(page, size, sortBy, sortDirection))
                .build();
    }

    /**
     * Lấy danh sách sản phẩm theo category
     */
    @GetMapping("/category/type/{categoryType}")
    public ApiResponse<PageResponseDTO<ProductListResponseDTO>> getByCategoryType(
            @PathVariable String categoryType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        return ApiResponse.<PageResponseDTO<ProductListResponseDTO>>builder()
                .result(productService.getProductsByCategoryType(categoryType, page, size, sortBy, sortDirection))
                .build();
    }

    @GetMapping("/category/{categoryId}")
    public ApiResponse<PageResponseDTO<ProductListResponseDTO>> getByCategoryId(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        return ApiResponse.<PageResponseDTO<ProductListResponseDTO>>builder()
                .result(productService.getProductsByCategoryId(categoryId, page, size, sortBy, sortDirection))
                .build();
    }

    /**
     * Lấy n sản phẩm mới nhất
     */
    @GetMapping("/latest")
    public ApiResponse<List<ProductListResponseDTO>> getLatest(@RequestParam(defaultValue = "10") int limit) {

        return ApiResponse.<List<ProductListResponseDTO>>builder()
                .result(productService.getLatestProducts(limit))
                .build();
    }

    /**
     * Tìm kiếm sản phẩm
     */
    @GetMapping("/search")
    public ApiResponse<PageResponseDTO<ProductListResponseDTO>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<String> brandNames,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        ProductSearchRequestDTO req = new ProductSearchRequestDTO();
        req.setKeyword(keyword);
        req.setBrandNames(brandNames);
        req.setCategoryIds(categoryIds);
        req.setMinPrice(minPrice);
        req.setMaxPrice(maxPrice);
        req.setPage(page);
        req.setSize(size);
        req.setSortBy(sortBy);
        req.setSortDirection(sortDirection);

        return ApiResponse.<PageResponseDTO<ProductListResponseDTO>>builder()
                .result(productService.searchProducts(req))
                .build();
    }

    @GetMapping("/variant/{id}")
    public ApiResponse<ProductResponseDTO> findByVariantId(@PathVariable Long id) {
        return ApiResponse.<ProductResponseDTO>builder()
                .result(productService.findByVariantId(id))
                .build();
    }
}
