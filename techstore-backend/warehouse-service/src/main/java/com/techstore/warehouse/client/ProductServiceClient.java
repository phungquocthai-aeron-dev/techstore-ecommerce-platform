package com.techstore.warehouse.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.techstore.warehouse.configuration.FileFeignConfig;
import com.techstore.warehouse.dto.response.ApiResponse;
import com.techstore.warehouse.dto.response.VariantInfo;

@FeignClient(name = "product-service", url = "${app.services.product}", configuration = FileFeignConfig.class)
public interface ProductServiceClient {

    @GetMapping("/variants/{variantId}")
    ApiResponse<VariantInfo> getVariantById(@PathVariable Long variantId);

    @GetMapping("/variants")
    ApiResponse<List<VariantInfo>> getVariantsByIds(@RequestParam List<Long> ids);
}
