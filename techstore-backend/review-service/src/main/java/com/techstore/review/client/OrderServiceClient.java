package com.techstore.review.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import com.techstore.review.configuration.FileFeignConfig;
import com.techstore.review.dto.response.ApiResponse;
import com.techstore.review.dto.response.OrderDetailResponse;

@FeignClient(name = "order-service", url = "${app.services.order}", configuration = FileFeignConfig.class)
public interface OrderServiceClient {

    @GetMapping("/orders/order-detail/{id}")
    ApiResponse<OrderDetailResponse> getOrderDetailById(@PathVariable Long id);

    @PutMapping("/orders/order-detail/{id}/reviewed")
    ApiResponse<Void> markOrderDetailReviewed(@PathVariable Long id);
}
