package com.techstore.quizgame.entity;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "user_points")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    // Tổng điểm tích lũy của user
    @Column(name = "total_points", nullable = false)
    private Integer totalPoints;
}
