package com.techstore.quizgame.entity;

import java.time.LocalDate;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(
        name = "user_daily_plays",
        // Composite unique key: mỗi user chỉ có 1 record per ngày
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "play_date"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDailyPlay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "play_date", nullable = false)
    private LocalDate playDate;

    // Số lượt đã chơi trong ngày
    @Column(name = "play_count", nullable = false)
    private Integer playCount;
}
