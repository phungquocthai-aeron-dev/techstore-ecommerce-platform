package com.techstore.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techstore.order.entity.Coupon;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {}
