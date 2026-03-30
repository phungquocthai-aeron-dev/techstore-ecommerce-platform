package com.techstore.quizgame.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerResponseDTO {
    private Long id;
    private String content;
    // KHÔNG trả về isCorrect để tránh gian lận
}
