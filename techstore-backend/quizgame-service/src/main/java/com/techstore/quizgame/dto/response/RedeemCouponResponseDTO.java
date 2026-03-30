package com.techstore.quizgame.dto.response;

import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedeemCouponResponseDTO {
    private Long userCouponId;
    private Long userId;
    private Long couponId;
    private String couponName;
    private int pointsSpent;
    private int remainingPoints;
    private LocalDateTime redeemedAt;
}
