package com.techstore.quizgame.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techstore.quizgame.entity.UserCoupon;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

    List<UserCoupon> findByUserIdOrderByRedeemedAtDesc(Long userId);

    boolean existsByUserIdAndCouponConfigId(Long userId, Long couponConfigId);
}
