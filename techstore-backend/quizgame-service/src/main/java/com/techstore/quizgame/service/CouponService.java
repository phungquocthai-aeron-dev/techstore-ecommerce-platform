package com.techstore.quizgame.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techstore.quizgame.client.CouponServiceClient;
import com.techstore.quizgame.dto.request.CouponConfigRequestDTO;
import com.techstore.quizgame.dto.request.RedeemCouponRequestDTO;
import com.techstore.quizgame.dto.response.CouponConfigResponseDTO;
import com.techstore.quizgame.dto.response.CouponResponse;
import com.techstore.quizgame.dto.response.RedeemCouponResponseDTO;
import com.techstore.quizgame.entity.CouponConfig;
import com.techstore.quizgame.entity.UserCoupon;
import com.techstore.quizgame.entity.UserPoint;
import com.techstore.quizgame.exception.AppException;
import com.techstore.quizgame.exception.ErrorCode;
import com.techstore.quizgame.repository.CouponConfigRepository;
import com.techstore.quizgame.repository.UserCouponRepository;
import com.techstore.quizgame.repository.UserPointRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {

    private final CouponConfigRepository couponConfigRepository;
    private final UserCouponRepository userCouponRepository;
    private final UserPointRepository userPointRepository;
    private final CouponServiceClient couponServiceClient;

    /**
     * Lấy danh sách coupon config local có thể đổi,
     * kèm thông tin chi tiết từ order-service và trạng thái user có đủ điểm không
     */
    public List<CouponConfigResponseDTO> getAvailableCoupons(Long userId) {
        int userPoints = userPointRepository
                .findByUserId(userId)
                .map(UserPoint::getTotalPoints)
                .orElse(0);

        // Lấy danh sách coupon đang ACTIVE từ order-service để hiển thị thông tin thật
        List<CouponResponse> orderCoupons;
        try {
            orderCoupons = couponServiceClient.getPrivateCoupons().getResult();
        } catch (Exception ex) {
            log.warn("Không thể lấy danh sách coupon từ order-service: {}", ex.getMessage());
            orderCoupons = List.of();
        }

        // Map order coupon theo id để lookup nhanh
        final List<CouponResponse> finalOrderCoupons = orderCoupons;

        List<CouponConfig> activeCoupons = couponConfigRepository.findByStatus("ACTIVE");

        return activeCoupons.stream()
                .map(config -> {
                    // Tìm thông tin coupon tương ứng từ order-service
                    CouponResponse orderCoupon = finalOrderCoupons.stream()
                            .filter(c -> c.getId().equals(config.getCouponId()))
                            .findFirst()
                            .orElse(null);

                    boolean canRedeem = userPoints >= config.getPointsRequired()
                            && config.getQuantity() != 0
                            && orderCoupon != null
                            && "ACTIVE".equals(orderCoupon.getStatus())
                            && orderCoupon.getEndDate().isAfter(LocalDateTime.now());

                    return CouponConfigResponseDTO.builder()
                            .id(config.getId())
                            .couponId(config.getCouponId())
                            // Ưu tiên lấy name từ order-service nếu có
                            .couponName(orderCoupon != null ? orderCoupon.getName() : config.getCouponName())
                            .description(config.getDescription())
                            .pointsRequired(config.getPointsRequired())
                            .quantity(config.getQuantity())
                            .status(config.getStatus())
                            .discountType(orderCoupon != null ? orderCoupon.getDiscountType() : null)
                            .discountValue(orderCoupon != null ? orderCoupon.getDiscountValue() : null)
                            .endDate(orderCoupon != null ? orderCoupon.getEndDate() : null)
                            .canRedeem(canRedeem)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Đổi coupon:
     * 1. Validate coupon config local (status, quantity)
     * 2. Validate điểm user
     * 3. Gọi order-service validate coupon (status, endDate, usageLimit)
     * 4. Trừ điểm + giảm quantity + assignCouponToCustomer
     */
    @Transactional
    public RedeemCouponResponseDTO redeemCoupon(RedeemCouponRequestDTO request) {
        Long userId = request.getUserId();
        Long couponConfigId = request.getCouponConfigId();

        // 1. Lấy config coupon trong DB local
        CouponConfig couponConfig = couponConfigRepository
                .findById(couponConfigId)
                .orElseThrow(() -> new AppException(ErrorCode.COUPON_NOT_FOUND));

        // 2. Kiểm tra coupon config local còn active không
        if (!"ACTIVE".equals(couponConfig.getStatus())) {
            throw new AppException(ErrorCode.COUPON_INACTIVE);
        }

        // 3. Kiểm tra còn số lượng không (quantity = -1 là unlimited)
        if (couponConfig.getQuantity() == 0) {
            throw new AppException(ErrorCode.COUPON_OUT_OF_STOCK);
        }

        // 4. Kiểm tra điểm user
        UserPoint userPoint =
                userPointRepository.findByUserId(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (userPoint.getTotalPoints() < couponConfig.getPointsRequired()) {
            throw new AppException(ErrorCode.INSUFFICIENT_POINTS);
        }

        // 5. Gọi order-service validate coupon còn dùng được không
        CouponResponse orderCoupon;
        try {
            orderCoupon = couponServiceClient
                    .getCouponById(couponConfig.getCouponId())
                    .getResult();
        } catch (Exception ex) {
            log.warn("Không thể validate coupon {} từ order-service: {}", couponConfig.getCouponId(), ex.getMessage());
            throw new AppException(ErrorCode.COUPON_VALIDATION_FAILED);
        }

        // 6. Kiểm tra status coupon bên order-service
        if (!"ACTIVE".equals(orderCoupon.getStatus())) {
            throw new AppException(ErrorCode.COUPON_INACTIVE);
        }

        // 7. Kiểm tra coupon chưa hết hạn
        if (orderCoupon.getEndDate() != null && orderCoupon.getEndDate().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.COUPON_EXPIRED);
        }

        // 8. Kiểm tra coupon chưa vượt usage limit
        if (orderCoupon.getUsageLimit() != null
                && orderCoupon.getUsedCount() != null
                && orderCoupon.getUsedCount() >= orderCoupon.getUsageLimit()) {
            throw new AppException(ErrorCode.COUPON_OUT_OF_STOCK);
        }

        // 9. Trừ điểm (atomic - chỉ trừ khi đủ điểm, tránh race condition)
        int updated = userPointRepository.deductPoints(userId, couponConfig.getPointsRequired());
        if (updated == 0) {
            throw new AppException(ErrorCode.INSUFFICIENT_POINTS);
        }

        // 10. Giảm quantity local nếu không phải unlimited
        if (couponConfig.getQuantity() > 0) {
            int quantityUpdated = couponConfigRepository.decrementQuantity(couponConfigId);
            if (quantityUpdated == 0) {
                // Race condition: hoàn điểm lại
                userPointRepository.addPoints(userId, couponConfig.getPointsRequired());
                throw new AppException(ErrorCode.COUPON_OUT_OF_STOCK);
            }
        }

        // 11. Gọi order-service gán coupon cho user
        try {
            couponServiceClient.assignCouponToCustomer(userId, couponConfig.getCouponId());
            log.info("Đã gán coupon {} cho user {}", couponConfig.getCouponId(), userId);
        } catch (Exception ex) {
            // Hoàn điểm + quantity nếu assign thất bại
            userPointRepository.addPoints(userId, couponConfig.getPointsRequired());
            if (couponConfig.getQuantity() > 0) {
                couponConfigRepository.incrementQuantity(couponConfigId);
            }
            log.error("Gán coupon thất bại, đã hoàn điểm cho user {}: {}", userId, ex.getMessage());
            throw new AppException(ErrorCode.COUPON_ASSIGN_FAILED);
        }

        // 12. Lưu user_coupon local để tracking
        UserCoupon userCoupon = UserCoupon.builder()
                .userId(userId)
                .couponId(couponConfig.getCouponId())
                .couponConfigId(couponConfigId)
                .pointsSpent(couponConfig.getPointsRequired())
                .build();
        userCoupon = userCouponRepository.save(userCoupon);

        int remainingPoints = userPoint.getTotalPoints() - couponConfig.getPointsRequired();

        log.info("User {} đổi coupon {} thành công. Điểm còn: {}", userId, couponConfig.getCouponId(), remainingPoints);

        return RedeemCouponResponseDTO.builder()
                .userCouponId(userCoupon.getId())
                .userId(userId)
                .couponId(couponConfig.getCouponId())
                .couponName(orderCoupon.getName())
                .pointsSpent(couponConfig.getPointsRequired())
                .remainingPoints(remainingPoints)
                .redeemedAt(userCoupon.getRedeemedAt())
                .build();
    }

    /**
     * Lịch sử đổi coupon của user
     */
    public List<UserCoupon> getUserCoupons(Long userId) {
        return userCouponRepository.findByUserIdOrderByRedeemedAtDesc(userId);
    }

    public CouponConfig createCouponConfig(CouponConfigRequestDTO request) {

        if (couponConfigRepository
                .findByCouponIdAndStatus(request.getCouponId(), "ACTIVE")
                .isPresent()) {
            throw new AppException(ErrorCode.COUPON_ALREADY_EXISTS);
        }

        CouponConfig coupon = CouponConfig.builder()
                .couponId(request.getCouponId())
                .couponName(request.getCouponName())
                .description(request.getDescription())
                .pointsRequired(request.getPointsRequired())
                .quantity(request.getQuantity())
                .status("ACTIVE")
                .build();

        return couponConfigRepository.save(coupon);
    }

    public CouponConfig updateCouponConfig(Long id, CouponConfigRequestDTO request) {

        CouponConfig coupon = couponConfigRepository
                .findByIdAndStatus(id, "ACTIVE")
                .orElseThrow(() -> new AppException(ErrorCode.COUPON_NOT_FOUND));

        coupon.setCouponId(request.getCouponId());
        coupon.setCouponName(request.getCouponName());
        coupon.setDescription(request.getDescription());
        coupon.setPointsRequired(request.getPointsRequired());
        coupon.setQuantity(request.getQuantity());
        coupon.setStatus(request.getStatus()); // cho phép ACTIVE / INACTIVE

        return couponConfigRepository.save(coupon);
    }

    @Transactional
    public void deleteCouponConfig(Long id) {

        CouponConfig coupon = couponConfigRepository
                .findByIdAndStatus(id, "ACTIVE")
                .orElseThrow(() -> new AppException(ErrorCode.COUPON_NOT_FOUND));

        coupon.setStatus("INACTIVE");
        couponConfigRepository.save(coupon);
    }

    public List<CouponConfig> getAllCouponConfigs() {
        return couponConfigRepository.findByStatus("ACTIVE");
    }

    public CouponConfig getCouponConfigById(Long id) {
        return couponConfigRepository
                .findByIdAndStatus(id, "ACTIVE")
                .orElseThrow(() -> new AppException(ErrorCode.COUPON_NOT_FOUND));
    }
}
