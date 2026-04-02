package com.techstore.quizgame.dto.response;

import java.util.List;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionDetailResponseDTO {

    private Long id;
    private String content;
    private Long topicId;
    private String topicName;
    private List<AnswerDetailResponseDTO> answers;
}
