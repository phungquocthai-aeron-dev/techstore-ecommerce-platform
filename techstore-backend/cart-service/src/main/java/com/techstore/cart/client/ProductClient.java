package com.techstore.cart.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.techstore.cart.configuration.FileFeignConfig;
import com.techstore.cart.response.ApiResponse;
import com.techstore.cart.response.VariantInfoResponse;

@FeignClient(name = "product-service", url = "${app.services.product}", configuration = FileFeignConfig.class)
public interface ProductClient {

    @GetMapping("/variants/detail/{variantId}")
    ApiResponse<VariantInfoResponse> getVariantById(@PathVariable Long variantId);

    @GetMapping("/variants")
    ApiResponse<List<VariantInfoResponse>> getVariantsByIds(@RequestParam List<Long> ids);
}
