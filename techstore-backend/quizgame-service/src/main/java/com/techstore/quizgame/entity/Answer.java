package com.techstore.quizgame.entity;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "answers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // Chỉ 1 answer đúng cho mỗi câu hỏi
    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;
}
