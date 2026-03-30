package com.techstore.order.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techstore.order.entity.CustomerCoupon;

public interface CustomerCouponRepository extends JpaRepository<CustomerCoupon, Long> {

    List<CustomerCoupon> findByCustomerId(Long customerId);

    Optional<CustomerCoupon> findByCustomerIdAndCouponId(Long customerId, Long couponId);

    boolean existsByCustomerIdAndCouponId(Long customerId, Long couponId);
}
