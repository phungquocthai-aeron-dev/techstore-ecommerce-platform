package com.techstore.quizgame.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.techstore.quizgame.entity.GameSession;

public interface GameSessionRepository extends JpaRepository<GameSession, Long> {

    List<GameSession> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Lấy tổng điểm user trong khoảng thời gian (cho leaderboard nếu cần)
    @Query("SELECT SUM(g.score) FROM GameSession g WHERE g.userId = :userId AND g.createdAt >= :from")
    Integer sumScoreByUserIdAndCreatedAtAfter(@Param("userId") Long userId, @Param("from") LocalDateTime from);
}
