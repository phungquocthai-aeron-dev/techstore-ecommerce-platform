package com.techstore.quizgame.dto.response;

import java.util.List;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionResponseDTO {
    private Long id;
    private String content;
    private String topic;
    private List<AnswerResponseDTO> answers;
}
