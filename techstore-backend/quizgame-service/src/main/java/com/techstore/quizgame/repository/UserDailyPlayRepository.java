package com.techstore.quizgame.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.techstore.quizgame.entity.UserDailyPlay;

public interface UserDailyPlayRepository extends JpaRepository<UserDailyPlay, Long> {

    Optional<UserDailyPlay> findByUserIdAndPlayDate(Long userId, LocalDate playDate);

    // Tăng play_count lên 1 (atomic update để tránh race condition)
    @Modifying
    @Query("UPDATE UserDailyPlay u SET u.playCount = u.playCount + 1 WHERE u.userId = :userId AND u.playDate = :date")
    int incrementPlayCount(@Param("userId") Long userId, @Param("date") LocalDate date);
}
