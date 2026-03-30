package com.techstore.chatbot.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.techstore.chatbot.configuration.FileFeignConfig;
import com.techstore.chatbot.dto.response.ApiResponse;
import com.techstore.chatbot.dto.response.CouponResponse;

@FeignClient(name = "order-service", url = "${app.services.order}", configuration = FileFeignConfig.class)
public interface CouponServiceClient {

    @GetMapping("/coupons")
    ApiResponse<List<CouponResponse>> getAllCoupons();

    @GetMapping("/coupons/{id}")
    ApiResponse<CouponResponse> getCouponById(@PathVariable Long id);

    @GetMapping("/coupons/available")
    ApiResponse<List<CouponResponse>> getAvailableCoupons();

    @GetMapping("/coupons/ids")
    ApiResponse<List<CouponResponse>> getCouponByIds(@RequestParam List<Long> ids);

    @GetMapping("/coupons/customer/{customerId}")
    ApiResponse<List<CouponResponse>> getCouponsByCustomer(@PathVariable Long customerId);

    @PostMapping("/coupons/customer/{customerId}/{couponId}")
    ApiResponse<Void> assignCouponToCustomer(@PathVariable Long customerId, @PathVariable Long couponId);

    @DeleteMapping("/coupons/customer/{customerId}/{couponId}")
    ApiResponse<Void> removeCouponFromCustomer(@PathVariable Long customerId, @PathVariable Long couponId);
}
