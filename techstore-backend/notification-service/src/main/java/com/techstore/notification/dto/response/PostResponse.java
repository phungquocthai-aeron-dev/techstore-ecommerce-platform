package com.techstore.notification.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PostResponse {
    String id;
    String userId;
    String content;
    String createdDate;
    String modifiedDate;
    String title;
    boolean isRead;
}
