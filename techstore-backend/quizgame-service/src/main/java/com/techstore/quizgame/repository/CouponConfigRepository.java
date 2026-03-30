package com.techstore.quizgame.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.techstore.quizgame.entity.CouponConfig;

public interface CouponConfigRepository extends JpaRepository<CouponConfig, Long> {

    List<CouponConfig> findByStatus(String status);

    Optional<CouponConfig> findByCouponId(Long couponId);

    // Giảm quantity đi 1 (chỉ giảm khi còn hàng)
    @Modifying
    @Query("UPDATE CouponConfig c SET c.quantity = c.quantity - 1 WHERE c.id = :id AND c.quantity > 0")
    int decrementQuantity(@Param("id") Long id);

    // Dùng để hoàn quantity khi assign thất bại
    @Modifying
    @Query("UPDATE CouponConfig c SET c.quantity = c.quantity + 1 WHERE c.id = :id")
    int incrementQuantity(@Param("id") Long id);
}
