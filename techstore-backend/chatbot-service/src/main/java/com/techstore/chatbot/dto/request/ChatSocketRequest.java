package com.techstore.chatbot.dto.request;

import lombok.Data;

@Data
public class ChatSocketRequest {
    private String message;
    private String sessionId;
    private String token;
}
