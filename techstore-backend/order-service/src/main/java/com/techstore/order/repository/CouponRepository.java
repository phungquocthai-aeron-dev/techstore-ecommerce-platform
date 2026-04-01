package com.techstore.order.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.techstore.order.entity.Coupon;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByName(String name);

    //    List<Coupon> findByStatusIgnoreCaseAndStartDateBeforeAndEndDateAfter(
    //            String status, LocalDateTime now1, LocalDateTime now2);

    @Query(
            """
			SELECT c FROM Coupon c
			WHERE LOWER(c.status) = LOWER(:status)
			AND c.couponType = :type
			AND c.startDate <= :now
			AND c.endDate >= :now
		""")
    List<Coupon> findValidPublicCoupons(
            @Param("status") String status, @Param("type") String type, @Param("now") LocalDateTime now);

    List<Coupon> findByIdIn(List<Long> ids);
}
