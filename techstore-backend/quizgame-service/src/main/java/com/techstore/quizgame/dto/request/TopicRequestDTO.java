package com.techstore.quizgame.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicRequestDTO {

    @NotBlank(message = "Tên chủ đề không được để trống")
    @Size(max = 100, message = "Tên chủ đề tối đa 100 ký tự")
    private String name;

    private String description;
}
