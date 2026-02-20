package com.techstore.order.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.techstore.order.dto.request.CouponRequest;
import com.techstore.order.dto.response.CouponResponse;
import com.techstore.order.entity.Coupon;

@Mapper(componentModel = "spring")
public interface CouponMapper {

    Coupon toEntity(CouponRequest request);

    CouponResponse toResponse(Coupon coupon);

    void updateEntity(CouponRequest request, @MappingTarget Coupon coupon);
}
