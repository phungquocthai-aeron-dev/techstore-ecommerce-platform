package com.techstore.review.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateReviewRequest {

    private String content;
    private Integer rating;
}
