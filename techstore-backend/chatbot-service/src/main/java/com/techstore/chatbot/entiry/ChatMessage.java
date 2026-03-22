package com.techstore.chatbot.entiry;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import com.techstore.chatbot.constant.MessageRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity lưu lịch sử chat.
 * Mỗi record là một tin nhắn (USER hoặc BOT).
 */
@Entity
@Table(name = "chat_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * userId từ JWT token (nullable nếu user chưa đăng nhập)
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * sessionId cho anonymous user (nullable nếu đã có userId)
     */
    @Column(name = "session_id")
    private String sessionId;

    /**
     * USER hoặc BOT
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private MessageRole role;

    /**
     * Nội dung tin nhắn
     */
    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    /**
     * Metadata dạng JSON string (type, data, ...)
     * VD: {"type":"PRODUCT_LIST","productCount":5}
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
