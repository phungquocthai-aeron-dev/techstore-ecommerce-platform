package com.techstore.chatbot.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techstore.chatbot.constant.MessageRole;
import com.techstore.chatbot.dto.response.ChatResponse;
import com.techstore.chatbot.entiry.ChatMessage;
import com.techstore.chatbot.repository.ChatMessageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service quản lý lịch sử chat.
 *
 * <p>Mỗi lượt chat lưu 2 record:
 * 1. USER message
 * 2. BOT response (kèm metadata dạng JSON)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatHistoryService {

    private final ChatMessageRepository chatMessageRepository;
    private final ObjectMapper objectMapper;

    /**
     * Lưu tin nhắn của USER.
     */
    @Transactional
    public ChatMessage saveUserMessage(String message, Long userId, String sessionId) {
        ChatMessage chatMessage = ChatMessage.builder()
                .role(MessageRole.USER)
                .message(message)
                .userId(userId)
                .sessionId(sessionId)
                .build();

        return chatMessageRepository.save(chatMessage);
    }

    /**
     * Lưu response của BOT.
     * Metadata lưu type và một số thông tin bổ sung.
     */
    @Transactional
    public ChatMessage saveBotResponse(ChatResponse response, Long userId, String sessionId) {
        String metadata = serializeMetadata(response);

        ChatMessage chatMessage = ChatMessage.builder()
                .role(MessageRole.BOT)
                .message(response.getMessage())
                .userId(userId)
                .sessionId(sessionId)
                .metadata(metadata)
                .build();

        return chatMessageRepository.save(chatMessage);
    }

    /**
     * Lấy lịch sử chat theo sessionId.
     */
    @Transactional(readOnly = true)
    public List<ChatMessage> getHistoryBySession(String sessionId) {
        return chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    /**
     * Lấy lịch sử chat theo userId.
     */
    @Transactional(readOnly = true)
    public List<ChatMessage> getHistoryByUser(Long userId) {
        return chatMessageRepository.findByUserIdOrderByCreatedAtAsc(userId);
    }

    /**
     * Serialize metadata của response thành JSON string.
     */
    private String serializeMetadata(ChatResponse response) {
        try {
            return objectMapper.writeValueAsString(new MetadataSnapshot(
                    response.getType() != null ? response.getType().name() : null, response.getMetadata()));
        } catch (JsonProcessingException ex) {
            log.warn("Failed to serialize metadata: {}", ex.getMessage());
            return null;
        }
    }

    /**
     * Inner record đại diện metadata được lưu vào DB.
     */
    public record MetadataSnapshot(String type, Object extra) {}
}
