package com.techstore.quizgame.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerResultDTO {
    private Long questionId;
    private String questionContent;
    private Long selectedAnswerId;
    private Long correctAnswerId;
    private boolean isCorrect;
}
