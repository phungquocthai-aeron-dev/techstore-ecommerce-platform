package com.techstore.review.dto.request;

import lombok.Data;

@Data
public class ReviewSearchRequest {
    private Long productId;
    private Long customerId;
    private Integer rating;
    private String status;
    private Boolean hasReply;
    private String keyword;
    private String sortBy = "createdAt";
    private String sortDir = "DESC";
    private int page = 0;
    private int size = 10;
}
