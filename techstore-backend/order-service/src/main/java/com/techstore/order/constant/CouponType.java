package com.techstore.order.constant;

import com.techstore.order.exception.AppException;
import com.techstore.order.exception.ErrorCode;

public enum CouponType {
    PUBLIC,
    PRIVATE;

    public static CouponType from(String type) {
        if (type == null) return PUBLIC;

        try {
            return CouponType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_COUPON_TYPE);
        }
    }
}
