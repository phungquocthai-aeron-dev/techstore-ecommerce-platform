package com.techstore.cart.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.techstore.cart.dto.request.CreateOrderRequest;
import com.techstore.cart.response.OrderResponse;

@FeignClient(name = "order-service", contextId = "orderClient", url = "${app.services.order}")
public interface OrderClient {

    @PostMapping("/api/orders")
    OrderResponse createOrder(@RequestBody CreateOrderRequest request);
}
