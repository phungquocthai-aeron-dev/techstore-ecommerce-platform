package com.techstore.cart.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.techstore.cart.response.VariantInfoResponse;

@FeignClient(name = "product-service", contextId = "productClient", url = "${app.services.product}", path = "/")
public interface ProductClient {

    @GetMapping("/variants/{variantId}")
    VariantInfoResponse getVariantInfo(@PathVariable("variantId") Long variantId);
}
