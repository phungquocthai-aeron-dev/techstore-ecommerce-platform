package com.techstore.quizgame.dto.response;

import java.util.List;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameResultResponseDTO {
    private Long sessionId;
    private Long userId;
    private int score; // Điểm lượt này
    private int totalPoints; // Tổng điểm tích lũy sau khi cộng
    private int correctCount; // Số câu đúng
    private int totalQuestions; // Tổng số câu (=10)
    private List<AnswerResultDTO> answerResults; // Chi tiết từng câu
}
