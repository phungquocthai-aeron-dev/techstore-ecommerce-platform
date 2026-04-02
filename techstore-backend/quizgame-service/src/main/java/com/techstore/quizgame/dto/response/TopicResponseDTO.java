package com.techstore.quizgame.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicResponseDTO {

    private Long id;
    private String name;
    private String description;
    private long questionCount;
}
