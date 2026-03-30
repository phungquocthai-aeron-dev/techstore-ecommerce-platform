package com.techstore.chat.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.techstore.chat.dto.request.ChatMessageRequest;
import com.techstore.chat.dto.response.ChatMessageResponse;
import com.techstore.chat.entity.ChatMessage;

@Mapper(componentModel = "spring")
public interface ChatMessageMapper {
    ChatMessageResponse toChatMessageResponse(ChatMessage chatMessage);

    ChatMessage toChatMessage(ChatMessageRequest request);

    List<ChatMessageResponse> toChatMessageResponses(List<ChatMessage> chatMessages);
}
