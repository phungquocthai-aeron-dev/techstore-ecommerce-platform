package com.techstore.quizgame.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.techstore.quizgame.dto.request.RedeemCouponRequestDTO;
import com.techstore.quizgame.dto.response.ApiResponse;
import com.techstore.quizgame.dto.response.CouponConfigResponseDTO;
import com.techstore.quizgame.dto.response.RedeemCouponResponseDTO;
import com.techstore.quizgame.entity.UserCoupon;
import com.techstore.quizgame.service.CouponService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    /**
     * GET /api/coupons/available?userId=1
     * Lấy danh sách coupon có thể đổi
     */
    @GetMapping("/available")
    public ApiResponse<List<CouponConfigResponseDTO>> getAvailableCoupons(@RequestParam Long userId) {
        return ApiResponse.<List<CouponConfigResponseDTO>>builder()
                .result(couponService.getAvailableCoupons(userId))
                .build();
    }

    /**
     * POST /api/coupons/redeem
     * Đổi coupon bằng điểm tích lũy
     */
    @PostMapping("/redeem")
    public ApiResponse<RedeemCouponResponseDTO> redeemCoupon(@RequestBody RedeemCouponRequestDTO request) {
        return ApiResponse.<RedeemCouponResponseDTO>builder()
                .result(couponService.redeemCoupon(request))
                .build();
    }

    /**
     * GET /api/coupons/history?userId=1
     * Lịch sử đổi coupon của user
     */
    @GetMapping("/history")
    public ApiResponse<List<UserCoupon>> getCouponHistory(@RequestParam Long userId) {
        return ApiResponse.<List<UserCoupon>>builder()
                .result(couponService.getUserCoupons(userId))
                .build();
    }
}
