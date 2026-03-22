package com.techstore.chatbot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techstore.chatbot.entiry.ChatMessage;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * Lấy lịch sử chat theo sessionId, sắp xếp theo thời gian tăng dần
     */
    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(String sessionId);

    /**
     * Lấy lịch sử chat theo userId, sắp xếp theo thời gian tăng dần
     */
    List<ChatMessage> findByUserIdOrderByCreatedAtAsc(Long userId);
}
