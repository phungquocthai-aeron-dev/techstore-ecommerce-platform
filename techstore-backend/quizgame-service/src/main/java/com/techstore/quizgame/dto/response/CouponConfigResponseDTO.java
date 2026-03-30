package com.techstore.quizgame.dto.response;

import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponConfigResponseDTO {
    private Long id;
    private Long couponId;
    private String couponName;
    private String description;
    private int pointsRequired;
    private int quantity;
    private String status;
    private boolean canRedeem;
    private String discountType;
    private Double discountValue;
    private LocalDateTime endDate;
}
