package com.techstore.quizgame.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedeemCouponRequestDTO {
    private Long userId;
    private Long couponConfigId; // ID trong bảng coupons_config
}
