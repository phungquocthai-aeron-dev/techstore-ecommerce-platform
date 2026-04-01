package com.techstore.chatbot.dto.response;

import com.techstore.chatbot.constant.ResponseType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSocketResponse {

    public enum Status {
        TYPING,
        DONE,
        ERROR
    }

    private Status status;
    private ResponseType type;
    private String message;
    private Object data;
    private Object metadata;

    // Factory helpers
    public static ChatSocketResponse typing() {
        return ChatSocketResponse.builder().status(Status.TYPING).build();
    }

    public static ChatSocketResponse from(ChatResponse response) {
        return ChatSocketResponse.builder()
                .status(Status.DONE)
                .type(response.getType())
                .message(response.getMessage())
                .data(response.getData())
                .metadata(response.getMetadata())
                .build();
    }

    public static ChatSocketResponse error(String message) {
        return ChatSocketResponse.builder()
                .status(Status.ERROR)
                .message(message)
                .build();
    }
}
