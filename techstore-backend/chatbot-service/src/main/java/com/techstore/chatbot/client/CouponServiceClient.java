package com.techstore.chatbot.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.techstore.chatbot.configuration.FileFeignConfig;
import com.techstore.chatbot.dto.response.ApiResponse;
import com.techstore.chatbot.dto.response.CouponResponse;

@FeignClient(name = "order-service", url = "${app.services.order}", configuration = FileFeignConfig.class)
public interface CouponServiceClient {

    // ===============================
    // GET ALL COUPONS
    // ===============================
    @GetMapping("/coupons")
    ApiResponse<List<CouponResponse>> getAllCoupons();

    // ===============================
    // GET COUPON BY ID
    // ===============================
    @GetMapping("/coupons/{id}")
    ApiResponse<CouponResponse> getCouponById(@PathVariable Long id);

    // ===============================
    // GET AVAILABLE COUPONS (QUAN TRỌNG)
    // ===============================
    @GetMapping("/coupons/available")
    ApiResponse<List<CouponResponse>> getAvailableCoupons();
}
