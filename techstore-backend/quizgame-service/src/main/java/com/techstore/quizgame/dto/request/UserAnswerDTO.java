package com.techstore.quizgame.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAnswerDTO {
    private Long questionId;
    private Long answerId; // ID đáp án user chọn
}
