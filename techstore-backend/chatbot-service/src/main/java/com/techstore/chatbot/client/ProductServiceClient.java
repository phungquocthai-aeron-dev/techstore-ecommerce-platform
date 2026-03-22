package com.techstore.chatbot.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.techstore.chatbot.configuration.FileFeignConfig;
import com.techstore.chatbot.dto.response.ApiResponse;
import com.techstore.chatbot.dto.response.BrandResponseDTO;
import com.techstore.chatbot.dto.response.CategoryResponseDTO;
import com.techstore.chatbot.dto.response.PageResponseDTO;
import com.techstore.chatbot.dto.response.ProductListResponseDTO;
import com.techstore.chatbot.dto.response.ProductResponseDTO;
import com.techstore.chatbot.dto.response.VariantInfo;
import com.techstore.chatbot.dto.response.VariantInfoWithStock;

@FeignClient(name = "product-service", url = "${app.services.product}", configuration = FileFeignConfig.class)
public interface ProductServiceClient {

    // ================= VARIANT =================

    @GetMapping("/variants/{variantId}")
    ApiResponse<VariantInfo> getVariantById(@PathVariable Long variantId);

    @GetMapping("/variants")
    ApiResponse<List<VariantInfo>> getVariantsByIds(@RequestParam List<Long> ids);

    @GetMapping("/variants/detail/{variantId}")
    ApiResponse<VariantInfoWithStock> getVariantDetailById(@PathVariable Long variantId);

    @GetMapping("/variants/search")
    ApiResponse<PageResponseDTO<VariantInfo>> searchVariants(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection);

    @GetMapping("/variants/all")
    ApiResponse<PageResponseDTO<VariantInfo>> getAllActiveVariants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection);

    @GetMapping("/products/{productId}/variants")
    ApiResponse<List<VariantInfo>> getVariantsByProductId(@PathVariable Long productId);

    @PostMapping("/variants/detail/batch")
    ApiResponse<List<VariantInfoWithStock>> getVariantsWithStock(@RequestBody List<Long> variantIds);

    // ================= PRODUCT =================

    @GetMapping("/products/variant/{id}")
    ApiResponse<ProductResponseDTO> getProductByVariantId(@PathVariable Long id);

    // ================= PRODUCT SEARCH =================
    @GetMapping("/products/search")
    ApiResponse<PageResponseDTO<ProductListResponseDTO>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<String> brandNames,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection);

    // ================= BRAND =================

    @GetMapping("/brands/{id}")
    ApiResponse<BrandResponseDTO> getBrandById(@PathVariable Long id);

    @GetMapping("/brands")
    ApiResponse<PageResponseDTO<BrandResponseDTO>> getAllBrands(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection);

    @GetMapping("/brands/status/{status}")
    ApiResponse<PageResponseDTO<BrandResponseDTO>> getBrandsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection);

    @GetMapping("/brands/search")
    ApiResponse<PageResponseDTO<BrandResponseDTO>> searchBrands(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection);

    // ================= CATEGORY =================

    @GetMapping("/categories/{id}")
    ApiResponse<CategoryResponseDTO> getCategoryById(@PathVariable Long id);

    @GetMapping("/categories")
    ApiResponse<PageResponseDTO<CategoryResponseDTO>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection);

    @GetMapping("/categories/type/{categoryType}")
    ApiResponse<PageResponseDTO<CategoryResponseDTO>> getCategoriesByType(
            @PathVariable String categoryType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection);

    @GetMapping("/categories/search")
    ApiResponse<PageResponseDTO<CategoryResponseDTO>> searchCategories(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoryType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection);
}
