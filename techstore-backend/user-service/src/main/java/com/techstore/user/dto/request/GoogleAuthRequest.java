package com.techstore.user.dto.request;

import lombok.Data;

@Data
public class GoogleAuthRequest {
    private String email;
    private String fullName;
    private String avatarUrl;
    private String providerId;
}
