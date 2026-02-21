package com.techstore.review.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.techstore.review.configuration.FileFeignConfig;
import com.techstore.review.dto.response.ApiResponse;
import com.techstore.review.dto.response.ProductResponseDTO;
import com.techstore.review.dto.response.VariantInfo;

@FeignClient(name = "product-service", url = "${app.services.product}", configuration = FileFeignConfig.class)
public interface ProductServiceClient {

    @GetMapping("/variants/{variantId}")
    ApiResponse<VariantInfo> getVariantById(@PathVariable Long variantId);

    @GetMapping("/variants")
    ApiResponse<List<VariantInfo>> getVariantsByIds(@RequestParam List<Long> ids);

    @GetMapping("/products/variant/{id}")
    ApiResponse<ProductResponseDTO> getProductByVariantId(@PathVariable Long id);
}
