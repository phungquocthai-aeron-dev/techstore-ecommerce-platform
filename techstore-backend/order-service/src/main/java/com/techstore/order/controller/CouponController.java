package com.techstore.order.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.techstore.order.dto.request.CouponCreateRequest;
import com.techstore.order.dto.request.CouponRequest;
import com.techstore.order.dto.response.ApiResponse;
import com.techstore.order.dto.response.CouponResponse;
import com.techstore.order.service.CouponService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    public ApiResponse<CouponResponse> create(@RequestBody CouponCreateRequest request) {

        return ApiResponse.<CouponResponse>builder()
                .result(couponService.create(request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<CouponResponse> getById(@PathVariable Long id) {

        return ApiResponse.<CouponResponse>builder()
                .result(couponService.getById(id))
                .build();
    }

    @GetMapping
    public ApiResponse<List<CouponResponse>> getAll() {

        return ApiResponse.<List<CouponResponse>>builder()
                .result(couponService.getAll())
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<CouponResponse> update(@PathVariable Long id, @RequestBody CouponRequest request) {

        return ApiResponse.<CouponResponse>builder()
                .result(couponService.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {

        couponService.delete(id);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/available")
    public ApiResponse<List<CouponResponse>> getAvailableCoupons() {

        return ApiResponse.<List<CouponResponse>>builder()
                .result(couponService.getAvailableCoupons())
                .build();
    }

    @GetMapping("/private")
    public ApiResponse<List<CouponResponse>> getPrivateCoupons() {

        return ApiResponse.<List<CouponResponse>>builder()
                .result(couponService.getPrivateCoupons())
                .build();
    }

    // GET /coupons/ids?ids=1,2,3
    @GetMapping("/ids")
    public ApiResponse<List<CouponResponse>> getByIds(@RequestParam List<Long> ids) {

        return ApiResponse.<List<CouponResponse>>builder()
                .result(couponService.getByIds(ids))
                .build();
    }

    // GET /coupons/customer/{customerId}
    @GetMapping("/customer/{customerId}")
    public ApiResponse<List<CouponResponse>> getCouponsByCustomer(@PathVariable Long customerId) {

        return ApiResponse.<List<CouponResponse>>builder()
                .result(couponService.getCouponsByCustomer(customerId))
                .build();
    }

    // POST /coupons/customer/{customerId}/{couponId}
    @PostMapping("/customer/{customerId}/{couponId}")
    public ApiResponse<Void> assignCouponToCustomer(@PathVariable Long customerId, @PathVariable Long couponId) {
        couponService.assignCouponToCustomer(customerId, couponId);
        return ApiResponse.<Void>builder().build();
    }

    // DELETE /coupons/customer/{customerId}/{couponId}
    @DeleteMapping("/customer/{customerId}/{couponId}")
    public ApiResponse<Void> removeCouponFromCustomer(@PathVariable Long customerId, @PathVariable Long couponId) {
        couponService.removeCouponFromCustomer(customerId, couponId);
        return ApiResponse.<Void>builder().build();
    }
}
