package com.techstore.chatbot.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.techstore.chatbot.entity.Conversation;

public interface ConversationRepository extends MongoRepository<Conversation, String> {

    Optional<Conversation> findByUserIdAndIsDeletedFalse(Long userId);

    Optional<Conversation> findBySessionIdAndIsDeletedFalse(String sessionId);
}
