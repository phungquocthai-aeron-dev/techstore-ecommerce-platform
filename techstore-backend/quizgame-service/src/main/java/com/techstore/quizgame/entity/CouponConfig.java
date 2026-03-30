package com.techstore.quizgame.entity;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "coupons_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID coupon ở order-service
    @Column(name = "coupon_id", nullable = false)
    private Long couponId;

    // Tên hiển thị coupon
    @Column(name = "coupon_name", nullable = false)
    private String couponName;

    // Mô tả
    @Column(columnDefinition = "TEXT")
    private String description;

    // Điểm cần để đổi
    @Column(name = "points_required", nullable = false)
    private Integer pointsRequired;

    // Số lượng còn lại (-1 = unlimited)
    @Column(nullable = false)
    private Integer quantity;

    // ACTIVE / INACTIVE
    @Column(nullable = false, length = 20)
    private String status;
}
