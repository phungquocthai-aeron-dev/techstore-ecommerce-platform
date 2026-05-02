package com.techstore.cart.infrastructure.feign.client;

import com.techstore.cart.infrastructure.feign.config.FeignConfig;
import com.techstore.cart.infrastructure.feign.dto.ApiResponse;
import com.techstore.cart.infrastructure.feign.dto.VariantInfoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "product-service",
        url = "${app.services.product}",
        configuration = FeignConfig.class
)
public interface ProductFeignClient {

    @GetMapping("/variants/detail/{variantId}")
    ApiResponse<VariantInfoDto> getVariantById(@PathVariable Long variantId);
}
