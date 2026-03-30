package com.techstore.order.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.techstore.event.dto.PostEvent;
import com.techstore.order.dto.request.CouponRequest;
import com.techstore.order.dto.response.CouponResponse;
import com.techstore.order.entity.Coupon;
import com.techstore.order.entity.CustomerCoupon;
import com.techstore.order.exception.AppException;
import com.techstore.order.exception.ErrorCode;
import com.techstore.order.mapper.CouponMapper;
import com.techstore.order.repository.CouponRepository;
import com.techstore.order.repository.CustomerCouponRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepo;
    private final CouponMapper couponMapper;
    private final CustomerCouponRepository customerCouponRepo;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @PreAuthorize("hasRole('ADMIN')")
    public CouponResponse create(CouponRequest request) {

        if (couponRepo.findByName(request.getName()).isPresent()) {
            throw new AppException(ErrorCode.COUPON_EXISTED);
        }

        validateDiscountType(request.getDiscountType());

        Coupon coupon = couponMapper.toEntity(request);
        coupon.setDiscountType(request.getDiscountType().toUpperCase());
        coupon.setStatus("ACTIVE");
        coupon.setUsedCount(0);

        PostEvent event = PostEvent.builder()
                .title("Khuyến mãi mới")
                .content(buildCouponContent(coupon))
                .userId("0")
                .build();

        kafkaTemplate.send("post-delivery", event);

        return couponMapper.toResponse(couponRepo.save(coupon));
    }

    public CouponResponse getById(Long id) {
        Coupon coupon = couponRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.COUPON_NOT_EXISTED));

        return couponMapper.toResponse(coupon);
    }

    public List<CouponResponse> getAll() {
        return couponRepo.findAll().stream().map(couponMapper::toResponse).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CouponResponse update(Long id, CouponRequest request) {

        Coupon coupon = couponRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.COUPON_NOT_EXISTED));

        validateDiscountType(request.getDiscountType());

        couponMapper.updateEntity(request, coupon);
        coupon.setDiscountType(request.getDiscountType().toUpperCase());

        return couponMapper.toResponse(couponRepo.save(coupon));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id) {

        Coupon coupon = couponRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.COUPON_NOT_EXISTED));

        couponRepo.delete(coupon);
    }

    public boolean isValid(Coupon coupon) {

        LocalDateTime now = LocalDateTime.now();

        return "ACTIVE".equalsIgnoreCase(coupon.getStatus())
                && now.isAfter(coupon.getStartDate())
                && now.isBefore(coupon.getEndDate())
                && (coupon.getUsageLimit() == null || coupon.getUsedCount() < coupon.getUsageLimit());
    }

    public List<CouponResponse> getAvailableCoupons() {

        LocalDateTime now = LocalDateTime.now();

        return couponRepo.findByStatusIgnoreCaseAndStartDateBeforeAndEndDateAfter("ACTIVE", now, now).stream()
                .filter(coupon -> coupon.getUsageLimit() == null || coupon.getUsedCount() < coupon.getUsageLimit())
                .map(couponMapper::toResponse)
                .toList();
    }

    // ── Lấy nhiều coupon theo danh sách ID ──────────────────────────────────────
    public List<CouponResponse> getByIds(List<Long> ids) {

        if (ids == null || ids.isEmpty()) {
            throw new AppException(ErrorCode.COUPON_IDS_EMPTY);
        }

        return couponRepo.findByIdIn(ids).stream().map(couponMapper::toResponse).toList();
    }

    // ── Lấy tất cả coupon của một khách hàng ────────────────────────────────────
    public List<CouponResponse> getCouponsByCustomer(Long customerId) {

        return customerCouponRepo.findByCustomerId(customerId).stream()
                .map(cc -> couponMapper.toResponse(cc.getCoupon()))
                .toList();
    }

    // ── Gán coupon cho khách hàng ────────────────────────────────────────────────
    @PreAuthorize("hasRole('ADMIN')")
    public void assignCouponToCustomer(Long customerId, Long couponId) {

        if (customerCouponRepo.existsByCustomerIdAndCouponId(customerId, couponId)) {
            throw new AppException(ErrorCode.CUSTOMER_COUPON_ALREADY_ASSIGNED);
        }

        Coupon coupon = couponRepo.findById(couponId).orElseThrow(() -> new AppException(ErrorCode.COUPON_NOT_EXISTED));

        CustomerCoupon customerCoupon = CustomerCoupon.builder()
                .customerId(customerId)
                .coupon(coupon)
                .assignedAt(LocalDateTime.now())
                .used(false)
                .build();

        customerCouponRepo.save(customerCoupon);
    }

    // ── Gỡ coupon khỏi khách hàng ────────────────────────────────────────────────
    @PreAuthorize("hasRole('ADMIN')")
    public void removeCouponFromCustomer(Long customerId, Long couponId) {

        CustomerCoupon customerCoupon = customerCouponRepo
                .findByCustomerIdAndCouponId(customerId, couponId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_COUPON_NOT_FOUND));

        customerCouponRepo.delete(customerCoupon);
    }

    private void validateDiscountType(String type) {

        if (type == null) {
            throw new AppException(ErrorCode.INVALID_DISCOUNT_TYPE);
        }

        if (!type.equalsIgnoreCase("PERCENT") && !type.equalsIgnoreCase("FIXED")) {

            throw new AppException(ErrorCode.INVALID_DISCOUNT_TYPE);
        }
    }

    private String buildCouponContent(Coupon coupon) {

        String discountValue = coupon.getDiscountType().equals("PERCENT")
                ? coupon.getDiscountValue() + "%"
                : coupon.getDiscountValue() + "đ";

        String minOrder = coupon.getMinOrderValue() != null ? " cho đơn từ " + coupon.getMinOrderValue() + "đ" : "";

        String maxDiscount = coupon.getMaxDiscount() != null ? ", giảm tối đa " + coupon.getMaxDiscount() + "đ" : "";

        String usageLimit = coupon.getUsageLimit() != null
                ? " Áp dụng cho " + coupon.getUsageLimit() + " khách hàng đầu tiên."
                : " Số lượng có hạn.";

        String expiry = coupon.getEndDate() != null
                ? " Hạn sử dụng đến " + coupon.getEndDate().toLocalDate() + "."
                : "";

        return "Ưu đãi mới! Voucher " + coupon.getName()
                + " giảm " + discountValue
                + minOrder
                + maxDiscount + "."
                + usageLimit
                + expiry
                + " Nhanh tay sử dụng ngay!";
    }
}
