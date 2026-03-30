package com.techstore.quizgame.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "user_coupons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // coupon_id từ order-service
    @Column(name = "coupon_id", nullable = false)
    private Long couponId;

    @Column(name = "coupon_config_id", nullable = false)
    private Long couponConfigId;

    // Điểm đã tiêu để đổi
    @Column(name = "points_spent", nullable = false)
    private Integer pointsSpent;

    @Column(name = "redeemed_at", nullable = false)
    private LocalDateTime redeemedAt;

    @PrePersist
    public void prePersist() {
        this.redeemedAt = LocalDateTime.now();
    }
}
