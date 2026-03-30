package com.techstore.chat.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.techstore.chat.entity.WebSocketSession;

@Repository
public interface WebSocketSessionRepository extends MongoRepository<WebSocketSession, String> {

    void deleteBySocketSessionId(String socketId);

    List<WebSocketSession> findAllByUserIdIn(List<String> userIds);

    @Query("{ $or: ?0 }")
    List<WebSocketSession> findByUserIdAndUserTypePairs(List<Map<String, String>> conditions);
}
