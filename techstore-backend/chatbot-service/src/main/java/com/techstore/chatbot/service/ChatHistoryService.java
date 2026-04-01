package com.techstore.chatbot.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.techstore.chatbot.constant.MessageRole;
import com.techstore.chatbot.dto.response.ChatResponse;
import com.techstore.chatbot.entity.ChatMessage;
import com.techstore.chatbot.entity.Conversation;
import com.techstore.chatbot.repository.ChatMessageRepository;
import com.techstore.chatbot.repository.ConversationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatHistoryService {

    private final ChatMessageRepository chatMessageRepository;
    private final ConversationRepository conversationRepository;

    // ─── Lấy hoặc tạo Conversation ──────────────────────────────────────────

    /**
     * Tìm conversation hiện tại theo userId hoặc sessionId.
     * Nếu chưa có → tạo mới.
     */
    public Conversation getOrCreateConversation(Long userId, String sessionId) {
        // Ưu tiên userId (user đã đăng nhập)
        if (userId != null) {
            return conversationRepository
                    .findByUserIdAndIsDeletedFalse(userId)
                    .orElseGet(() -> conversationRepository.save(Conversation.builder()
                            .userId(userId)
                            .sessionId(sessionId)
                            .build()));
        }

        // Fallback: sessionId (anonymous user)
        if (sessionId != null && !sessionId.isBlank()) {
            return conversationRepository
                    .findBySessionIdAndIsDeletedFalse(sessionId)
                    .orElseGet(() -> conversationRepository.save(
                            Conversation.builder().sessionId(sessionId).build()));
        }

        // Không có cả hai → tạo conversation tạm (không nên xảy ra)
        log.warn("[ChatHistory] No userId or sessionId — creating orphan conversation");
        return conversationRepository.save(Conversation.builder().build());
    }

    // ─── Lưu tin nhắn ───────────────────────────────────────────────────────

    public ChatMessage saveUserMessage(String message, Long userId, String sessionId) {
        String conversationId = getOrCreateConversation(userId, sessionId).getId();

        ChatMessage chatMessage = ChatMessage.builder()
                .conversationId(conversationId)
                .role(MessageRole.USER.name())
                .message(message)
                .build();

        return chatMessageRepository.save(chatMessage);
    }

    public ChatMessage saveBotResponse(ChatResponse response, Long userId, String sessionId) {
        String conversationId = getOrCreateConversation(userId, sessionId).getId();

        Map<String, Object> metadata = buildMetadata(response);

        ChatMessage chatMessage = ChatMessage.builder()
                .conversationId(conversationId)
                .role(MessageRole.BOT.name())
                .message(response.getMessage())
                .metadata(metadata)
                .build();

        // Cập nhật updatedAt của conversation
        conversationRepository.findById(conversationId).ifPresent(conv -> {
            conv.setUpdatedAt(LocalDateTime.now());
            conversationRepository.save(conv);
        });

        return chatMessageRepository.save(chatMessage);
    }

    // ─── Lấy lịch sử ────────────────────────────────────────────────────────

    public List<ChatMessage> getHistoryBySession(String sessionId) {
        return conversationRepository
                .findBySessionIdAndIsDeletedFalse(sessionId)
                .map(conv -> chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conv.getId()))
                .orElse(List.of());
    }

    public List<ChatMessage> getHistoryByUser(Long userId) {
        return conversationRepository
                .findByUserIdAndIsDeletedFalse(userId)
                .map(conv -> chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conv.getId()))
                .orElse(List.of());
    }

    // ─── Helper ─────────────────────────────────────────────────────────────

    private Map<String, Object> buildMetadata(ChatResponse response) {
        if (response.getType() == null && response.getMetadata() == null) return null;
        return Map.of(
                "type", response.getType() != null ? response.getType().name() : "UNKNOWN",
                "extra", response.getMetadata() != null ? response.getMetadata() : Map.of());
    }

    public record MetadataSnapshot(String type, Object extra) {}
}
