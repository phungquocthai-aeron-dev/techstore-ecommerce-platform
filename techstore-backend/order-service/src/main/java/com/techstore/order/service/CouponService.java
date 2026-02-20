package com.techstore.order.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.techstore.order.dto.request.CouponRequest;
import com.techstore.order.dto.response.CouponResponse;
import com.techstore.order.entity.Coupon;
import com.techstore.order.exception.AppException;
import com.techstore.order.exception.ErrorCode;
import com.techstore.order.mapper.CouponMapper;
import com.techstore.order.repository.CouponRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepo;
    private final CouponMapper couponMapper;

    @PreAuthorize("hasRole('ADMIN')")
    public CouponResponse create(CouponRequest request) {

        if (couponRepo.findByName(request.getName()).isPresent()) {
            throw new AppException(ErrorCode.COUPON_EXISTED);
        }

        Coupon coupon = couponMapper.toEntity(request);
        coupon.setStatus("ACTIVE");
        coupon.setUsedCount(0);

        return couponMapper.toResponse(couponRepo.save(coupon));
    }

    public CouponResponse getById(Long id) {
        return couponMapper.toResponse(
                couponRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.COUPON_NOT_EXISTED)));
    }

    public List<CouponResponse> getAll() {
        return couponRepo.findAll().stream().map(couponMapper::toResponse).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CouponResponse update(Long id, CouponRequest request) {

        Coupon coupon = couponRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.COUPON_NOT_EXISTED));

        couponMapper.updateEntity(request, coupon);

        return couponMapper.toResponse(couponRepo.save(coupon));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id) {
        Coupon coupon = couponRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.COUPON_NOT_EXISTED));

        couponRepo.delete(coupon);
    }

    public boolean isValid(Coupon coupon) {
        LocalDateTime now = LocalDateTime.now();

        return coupon.getStatus().equals("ACTIVE")
                && now.isAfter(coupon.getStartDate())
                && now.isBefore(coupon.getEndDate())
                && coupon.getUsedCount() < coupon.getUsageLimit();
    }
}
