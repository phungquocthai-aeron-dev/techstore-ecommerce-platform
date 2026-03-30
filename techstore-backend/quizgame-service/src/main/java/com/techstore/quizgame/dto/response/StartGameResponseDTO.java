package com.techstore.quizgame.dto.response;

import java.util.List;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartGameResponseDTO {
    private Long userId;
    private int remainingPlays; // Số lượt còn lại trong ngày
    private int totalPlaysToday; // Tổng số lượt đã chơi hôm nay
    private List<QuestionResponseDTO> questions;
}
