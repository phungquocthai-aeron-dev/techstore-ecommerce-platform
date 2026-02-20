package com.techstore.order.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

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
    public ApiResponse<CouponResponse> create(@RequestBody CouponRequest request) {

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
}
