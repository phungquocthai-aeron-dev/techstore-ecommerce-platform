package com.techstore.chatbot.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

/**
 * Request body cho API POST /api/chat
 */
@Data
public class ChatRequest {

    /**
     * Nội dung tin nhắn của user
     */
    @NotBlank(message = "Message không được để trống")
    private String message;

    /**
     * Session ID cho anonymous user (frontend tự sinh UUID và giữ lại)
     * Nếu user đã đăng nhập thì sessionId vẫn có thể gửi, nhưng hệ thống ưu tiên userId từ JWT
     */
    private String sessionId;
}
