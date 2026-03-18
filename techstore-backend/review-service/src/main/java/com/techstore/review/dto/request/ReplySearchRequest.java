package com.techstore.review.dto.request;

import lombok.Data;

@Data
public class ReplySearchRequest {
    private Long staffId;
    private String status;
    private String keyword;
    private String sortDir = "DESC";
    private int page = 0;
    private int size = 10;
}
