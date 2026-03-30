package com.techstore.chat.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.techstore.chat.dto.response.ConversationResponse;
import com.techstore.chat.entity.Conversation;

@Mapper(componentModel = "spring")
public interface ConversationMapper {
    ConversationResponse toConversationResponse(Conversation conversation);

    List<ConversationResponse> toConversationResponseList(List<Conversation> conversations);
}
