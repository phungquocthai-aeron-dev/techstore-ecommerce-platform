package com.techstore.quizgame.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.techstore.quizgame.dto.request.CouponConfigRequestDTO;
import com.techstore.quizgame.dto.request.RedeemCouponRequestDTO;
import com.techstore.quizgame.dto.response.ApiResponse;
import com.techstore.quizgame.dto.response.CouponConfigResponseDTO;
import com.techstore.quizgame.dto.response.RedeemCouponResponseDTO;
import com.techstore.quizgame.entity.CouponConfig;
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

    /**
     * ADMIN - tạo coupon config
     */
    @PostMapping
    public ApiResponse<CouponConfig> createCoupon(@RequestBody CouponConfigRequestDTO request) {
        return ApiResponse.<CouponConfig>builder()
                .result(couponService.createCouponConfig(request))
                .build();
    }

    /**
     * ADMIN - cập nhật coupon config
     */
    @PutMapping("/{id}")
    public ApiResponse<CouponConfig> updateCoupon(@PathVariable Long id, @RequestBody CouponConfigRequestDTO request) {

        return ApiResponse.<CouponConfig>builder()
                .result(couponService.updateCouponConfig(id, request))
                .build();
    }

    /**
     * ADMIN - xóa coupon config
     */
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteCoupon(@PathVariable Long id) {
        couponService.deleteCouponConfig(id);
        return ApiResponse.<String>builder().result("Deleted successfully").build();
    }

    /**
     * ADMIN - lấy tất cả coupon config
     */
    @GetMapping
    public ApiResponse<List<CouponConfig>> getAllCoupons() {
        return ApiResponse.<List<CouponConfig>>builder()
                .result(couponService.getAllCouponConfigs())
                .build();
    }

    /**
     * ADMIN - lấy chi tiết 1 coupon config
     */
    @GetMapping("/{id}")
    public ApiResponse<CouponConfig> getCouponById(@PathVariable Long id) {
        return ApiResponse.<CouponConfig>builder()
                .result(couponService.getCouponConfigById(id))
                .build();
    }
}
