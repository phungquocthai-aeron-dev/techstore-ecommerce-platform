package com.techstore.quizgame.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.techstore.quizgame.entity.UserPoint;

public interface UserPointRepository extends JpaRepository<UserPoint, Long> {

    Optional<UserPoint> findByUserId(Long userId);

    // Cộng điểm
    @Modifying
    @Query("UPDATE UserPoint u SET u.totalPoints = u.totalPoints + :points WHERE u.userId = :userId")
    int addPoints(@Param("userId") Long userId, @Param("points") int points);

    // Trừ điểm (chỉ trừ khi đủ điểm)
    @Modifying
    @Query(
            "UPDATE UserPoint u SET u.totalPoints = u.totalPoints - :points WHERE u.userId = :userId AND u.totalPoints >= :points")
    int deductPoints(@Param("userId") Long userId, @Param("points") int points);
}
