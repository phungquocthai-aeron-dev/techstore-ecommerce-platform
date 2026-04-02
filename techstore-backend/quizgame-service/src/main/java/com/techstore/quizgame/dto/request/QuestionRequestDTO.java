package com.techstore.quizgame.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionRequestDTO {

    @NotBlank(message = "Nội dung câu hỏi không được để trống")
    private String content;

    @NotNull(message = "Phải chọn chủ đề")
    private Long topicId;

    @NotEmpty(message = "Câu hỏi phải có ít nhất 1 đáp án")
    @Size(min = 2, max = 6, message = "Số đáp án phải từ 2 đến 6")
    @Valid
    private List<AnswerRequestDTO> answers;
}
