package com.techstore.warehouse.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.techstore.warehouse.dto.response.ApiResponse;
import com.techstore.warehouse.dto.response.VariantInfo;

@FeignClient(name = "product-service", url = "${product.service.url:http://localhost:8082}")
public interface ProductServiceClient {

    @GetMapping("/variants/{variantId}")
    ApiResponse<VariantInfo> getVariantById(@PathVariable Long variantId);
}
