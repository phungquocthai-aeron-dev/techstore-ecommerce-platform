package com.techstore.chatbot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponse {
    private String sessionId; // UUID để dùng
    private String sessionToken; // JWT ký bởi server để verify
    private long expiresAt; // timestamp hết hạn
}
