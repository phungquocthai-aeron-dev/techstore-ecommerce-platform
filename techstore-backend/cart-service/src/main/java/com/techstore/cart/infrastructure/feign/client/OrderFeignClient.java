package com.techstore.cart.infrastructure.feign.client;

import com.techstore.cart.infrastructure.feign.config.FeignConfig;
import com.techstore.cart.infrastructure.feign.dto.CreateOrderRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "order-service",
        contextId = "orderClient",
        url = "${app.services.order}",
        configuration = FeignConfig.class
)
public interface OrderFeignClient {

    @PostMapping("/api/orders")
    void createOrder(@RequestBody CreateOrderRequest request);
}
