package com.techstore.chat.entity;

import java.time.Instant;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "web_socket_session")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WebSocketSession {
    @MongoId
    String id;

    String socketSessionId;

    String userType;

    String userId;

    Instant createdAt;
}
