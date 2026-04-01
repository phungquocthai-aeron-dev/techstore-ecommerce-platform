package com.techstore.order.constant;

import com.techstore.order.exception.AppException;
import com.techstore.order.exception.ErrorCode;

public enum DiscountType {
    PERCENT,
    FIXED;

    public static DiscountType from(String type) {
        if (type == null) {
            throw new AppException(ErrorCode.INVALID_DISCOUNT_TYPE);
        }

        try {
            return DiscountType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_DISCOUNT_TYPE);
        }
    }
}
