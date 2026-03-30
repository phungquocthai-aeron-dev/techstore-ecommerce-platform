package com.techstore.order.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "customer_coupon", uniqueConstraints = @UniqueConstraint(columnNames = {"customer_id", "coupon_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Builder.Default
    @Column(name = "used")
    private boolean used = false;
}
