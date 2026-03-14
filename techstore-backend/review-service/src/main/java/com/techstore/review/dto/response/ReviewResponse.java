package com.techstore.review.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ReviewResponse {

    private Long id;
    private String content;
    private Integer rating;
    private LocalDateTime createdAt;
    private String status;

    private Long productId;
    private Long variantId;
    private Long orderDetailId;
    private Long customerId;
    private CustomerResponse customer;
    private Boolean reviewed;

    private ReplyResponse reply;
}
