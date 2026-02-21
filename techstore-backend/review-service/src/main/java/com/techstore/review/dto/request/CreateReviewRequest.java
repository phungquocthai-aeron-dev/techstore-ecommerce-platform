package com.techstore.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateReviewRequest {

    private Long orderDetailId;
    private String content;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;
}
