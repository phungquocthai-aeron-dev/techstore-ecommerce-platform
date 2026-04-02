package com.techstore.quizgame.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerRequestDTO {

    @NotBlank(message = "Nội dung đáp án không được để trống")
    private String content;

    @NotNull(message = "Phải xác định đáp án đúng/sai")
    private Boolean isCorrect;
}
