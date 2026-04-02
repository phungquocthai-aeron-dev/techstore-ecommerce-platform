package com.techstore.quizgame.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerDetailResponseDTO {

    private Long id;
    private String content;
    private Boolean isCorrect;
}
