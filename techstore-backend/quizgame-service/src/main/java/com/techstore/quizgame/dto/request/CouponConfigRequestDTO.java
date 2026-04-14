package com.techstore.quizgame.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponConfigRequestDTO {

    private Long couponId;
    private String couponName;
    private String description;
    private Integer pointsRequired;
    private Integer quantity;
    private String status; // ACTIVE / INACTIVE
}
