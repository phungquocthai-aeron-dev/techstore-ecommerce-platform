package com.techstore.order.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techstore.order.entity.Coupon;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByName(String name);

    List<Coupon> findByStatusIgnoreCaseAndStartDateBeforeAndEndDateAfter(
            String status, LocalDateTime now1, LocalDateTime now2);
}
