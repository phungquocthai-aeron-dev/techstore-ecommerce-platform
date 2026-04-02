package com.techstore.quizgame.entity;

import java.util.List;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // Giữ lại field String topic để tương thích ngược (hoặc có thể xóa nếu không cần)
    @Column(length = 100)
    private String topicName; // đổi tên để tránh xung đột

    // Quan hệ FK tới bảng topics
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Answer> answers;
}
