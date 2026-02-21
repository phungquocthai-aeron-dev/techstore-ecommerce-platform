package com.techstore.review.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReplyResponse {

    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private String status;
    private Long staffId;
}
