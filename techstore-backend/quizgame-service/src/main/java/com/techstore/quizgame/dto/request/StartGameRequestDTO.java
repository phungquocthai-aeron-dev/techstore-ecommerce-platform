package com.techstore.quizgame.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartGameRequestDTO {
    private Long userId;
}
