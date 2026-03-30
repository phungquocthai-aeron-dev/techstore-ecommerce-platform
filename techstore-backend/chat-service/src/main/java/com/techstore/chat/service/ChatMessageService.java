package com.techstore.chat.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.corundumstudio.socketio.SocketIOServer;
import com.techstore.chat.constant.UserType;
import com.techstore.chat.dto.request.ChatMessageRequest;
import com.techstore.chat.dto.response.ChatMessageResponse;
import com.techstore.chat.entity.ChatMessage;
import com.techstore.chat.entity.Conversation;
import com.techstore.chat.entity.ParticipantInfo;
import com.techstore.chat.entity.WebSocketSession;
import com.techstore.chat.exception.AppException;
import com.techstore.chat.exception.ErrorCode;
import com.techstore.chat.mapper.ChatMessageMapper;
import com.techstore.chat.repository.ChatMessageRepository;
import com.techstore.chat.repository.ConversationRepository;
import com.techstore.chat.repository.WebSocketSessionRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatMessageService {
    SocketIOServer socketIOServer;

    ChatMessageRepository chatMessageRepository;
    ConversationRepository conversationRepository;
    WebSocketSessionRepository webSocketSessionRepository;
    ChatMessageMapper chatMessageMapper;

    public List<ChatMessageResponse> getMessages(String conversationId, String userType) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info(userId);

        UserType type;
        try {
            type = UserType.valueOf(userType);
        } catch (Exception e) {
            throw new AppException(ErrorCode.INVALID_USER_TYPE);
        }

        Conversation conversation = conversationRepository
                .findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        boolean isParticipant = conversation.getParticipants().stream()
                .anyMatch(p -> userId.equals(p.getUserId()) && type.name().equals(p.getUserType()));

        if (!isParticipant) {
            throw new AppException(ErrorCode.CONVERSATION_ACCESS_DENIED);
        }

        return chatMessageRepository.findAllByConversationIdOrderByCreatedDateAsc(conversationId).stream()
                .map(msg -> toChatMessageResponse(msg, userType))
                .toList();
    }

    //    public ChatMessageResponse create(ChatMessageRequest request) {
    //        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
    //
    //        UserType userType;
    //        try {
    //            userType = UserType.valueOf(request.getUserType());
    //        } catch (Exception e) {
    //            throw new AppException(ErrorCode.INVALID_USER_TYPE);
    //        }
    //
    //        // Validate conversationId
    //        Conversation conversation = conversationRepository
    //                .findById(request.getConversationId())
    //                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));
    //
    //        ParticipantInfo sender = conversation.getParticipants().stream()
    //                .filter(p -> userId.equals(p.getUserId()) && userType.name().equals(p.getUserType()))
    //                .findFirst()
    //                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_ACCESS_DENIED));
    //
    //        // Build Chat message
    //        ChatMessage chatMessage = chatMessageMapper.toChatMessage(request);
    //        chatMessage.setSender(sender);
    //        chatMessage.setCreatedDate(Instant.now());
    //
    //        // Save (ONLY ONCE)
    //        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
    //
    //        // Prepare socket sessions
    //        List<String> userIds = conversation.getParticipants().stream()
    //                .map(ParticipantInfo::getUserId)
    //                .toList();
    //
    //        Map<String, WebSocketSession> webSocketSessions =
    // webSocketSessionRepository.findAllByUserIdIn(userIds).stream()
    //                .collect(Collectors.toMap(WebSocketSession::getSocketSessionId, Function.identity(), (a, b) -> {
    //                    log.warn("Duplicate socket sessionId: {}", a.getSocketSessionId());
    //                    return a;
    //                }));
    //
    //        //        // Push socket event
    //        //        socketIOServer.getAllClients().forEach(client -> {
    //        //            var webSocketSession = webSocketSessions.get(client.getSessionId().toString());
    //        //
    //        //            if (Objects.nonNull(webSocketSession)) {
    //        //                //                try {
    //        //                ChatMessageResponse clone = chatMessageMapper.toChatMessageResponse(savedMessage);
    //        //
    //        //                boolean isMe = webSocketSession.getUserId().equals(userId)
    //        //                        && webSocketSession.getUserType().equals(userType.name());
    //        //
    //        //                clone.setMe(isMe);
    //        //
    //        //                //                    String message = objectMapper.writeValueAsString(clone);
    //        //                client.sendEvent("new_message", clone);
    //        //
    //        //                //                } catch (JsonProcessingException e) {
    //        //                //                    log.error("Send socket message error", e);
    //        //                //                }
    //        //            }
    //        //        });
    //
    //        log.info("Begin");
    //        //        socketIOServer.getAllClients().forEach(client -> {
    //        //            var webSocketSession = webSocketSessions.values().stream()
    //        //                    .filter(s ->
    //        //                            s.getSocketSessionId().equals(client.getSessionId().toString()))
    //        //                    .findFirst()
    //        //                    .orElse(null);
    //        //
    //        //            if (Objects.nonNull(webSocketSession)) {
    //        //                ChatMessageResponse clone = chatMessageMapper.toChatMessageResponse(savedMessage);
    //        //
    //        //                boolean isMe = webSocketSession.getUserId().equals(userId)
    //        //                        && webSocketSession.getUserType().equals(userType.name());
    //        //
    //        //                clone.setMe(isMe);
    //        //
    //        //                log.info("Signed");
    //        //
    //        //                client.sendEvent("new_message", clone);
    //        //            }
    //        //        });
    //
    //        for (ParticipantInfo p : conversation.getParticipants()) {
    //
    //            String room = p.getUserId() + "_" + p.getUserType();
    //
    //            ChatMessageResponse clone = chatMessageMapper.toChatMessageResponse(savedMessage);
    //
    //            boolean isMe = p.getUserId().equals(userId) && p.getUserType().equals(userType.name());
    //
    //            clone.setMe(isMe);
    //
    //            log.info("SEND TO ROOM: {}", room);
    //
    //            socketIOServer.getRoomOperations(room).sendEvent("new_message", clone);
    //            log.info(
    //                    "Clients in room {}: {}",
    //                    room,
    //                    socketIOServer.getRoomOperations(room).getClients().size());
    //        }
    //
    //        return toChatMessageResponse(savedMessage, userType.name());
    //    }

    public ChatMessageResponse create(ChatMessageRequest request) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("USERID: " + userId);

        UserType userType;
        try {
            userType = UserType.valueOf(request.getUserType());
        } catch (Exception e) {
            throw new AppException(ErrorCode.INVALID_USER_TYPE);
        }

        // Validate conversationId
        Conversation conversation = conversationRepository
                .findById(request.getConversationId())
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        ParticipantInfo sender = conversation.getParticipants().stream()
                .filter(p -> userId.equals(p.getUserId()) && userType.name().equals(p.getUserType()))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_ACCESS_DENIED));

        // Build Chat message
        ChatMessage chatMessage = chatMessageMapper.toChatMessage(request);
        chatMessage.setSender(sender);
        chatMessage.setCreatedDate(Instant.now());

        // Save (ONLY ONCE)
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        // Prepare socket sessions
        //        List<String> userIds = conversation.getParticipants().stream()
        //                .map(ParticipantInfo::getUserId)
        //                .toList();

        List<Map<String, String>> conditions = conversation.getParticipants().stream()
                .map(p -> Map.of(
                        "userId", p.getUserId(),
                        "userType", p.getUserType()))
                .toList();

        log.info("Begin");

        List<WebSocketSession> sessions = webSocketSessionRepository.findByUserIdAndUserTypePairs(conditions);

        //        Map<String, WebSocketSession> webSocketSessions =
        // webSocketSessionRepository.findAllByUserIdIn(userIds).stream()
        //                .collect(Collectors.toMap(WebSocketSession::getSocketSessionId, Function.identity(), (a, b) ->
        // {
        //                    log.warn("Duplicate socket sessionId: {}", a.getSocketSessionId());
        //                    return a;
        //                }));

        Map<String, WebSocketSession> webSocketSessions = sessions.stream()
                .collect(Collectors.toMap(WebSocketSession::getSocketSessionId, Function.identity(), (a, b) -> {
                    log.warn("Duplicate socket sessionId: {}", a.getSocketSessionId());
                    return a;
                }));

        ChatMessageResponse chatMessageResponse = chatMessageMapper.toChatMessageResponse(chatMessage);

        // Push socket event
        socketIOServer.getAllClients().forEach(client -> {
            var webSocketSession = webSocketSessions.get(client.getSessionId().toString());
            log.info("Tìm session");
            log.info(client.getSessionId().toString());
            log.info(webSocketSession.getUserId() + " | " + webSocketSession.getUserType());

            if (Objects.nonNull(webSocketSession)) {

                String message = null;
                //                try {
                //                chatMessageResponse.setMe(webSocketSession.getUserId().equals(userId)
                //                        && webSocketSession.getUserType().equals(userType.name()));

                chatMessageResponse.setCreatedDate(chatMessage.getCreatedDate().toString());
                client.sendEvent("new_message", chatMessageResponse);
                log.info("Gửi message");
                log.info(message);
                //                } catch (JsonProcessingException e) {
                //                    throw new RuntimeException(e);
                //                }

                //                        //                try {
                //                        ChatMessageResponse clone =
                // chatMessageMapper.toChatMessageResponse(savedMessage);
                //
                //                        boolean isMe = webSocketSession.getUserId().equals(userId)
                //                                && webSocketSession.getUserType().equals(userType.name());
                //
                //                        clone.setMe(isMe);
                //
                //                        //                    String message = objectMapper.writeValueAsString(clone);
                //                        client.sendEvent("new_message", clone);
                //
                //                        //                } catch (JsonProcessingException e) {
                //                        //                    log.error("Send socket message error", e);
                //                        //                }
            }
        });

        //        for (ParticipantInfo p : conversation.getParticipants()) {
        //
        //            String room = p.getUserId() + "_" + p.getUserType();
        //
        //            ChatMessageResponse clone = chatMessageMapper.toChatMessageResponse(savedMessage);
        //
        //            boolean isMe = p.getUserId().equals(userId) && p.getUserType().equals(userType.name());
        //
        //            clone.setMe(isMe);
        //
        //            log.info("SEND TO ROOM: {}", room);
        //
        //            socketIOServer.getRoomOperations(room).sendEvent("new_message", clone);
        //            log.info(
        //                    "Clients in room {}: {}",
        //                    room,
        //                    socketIOServer.getRoomOperations(room).getClients().size());
        //        }

        return toChatMessageResponse(savedMessage, userType.name());
    }

    private ChatMessageResponse toChatMessageResponse(ChatMessage chatMessage, String userType) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        UserType type;
        try {
            type = UserType.valueOf(userType);
        } catch (Exception e) {
            throw new AppException(ErrorCode.INVALID_USER_TYPE);
        }

        var chatMessageResponse = chatMessageMapper.toChatMessageResponse(chatMessage);

        boolean isMe = userId.equals(chatMessage.getSender().getUserId())
                && type.name().equals(chatMessage.getSender().getUserType());

        chatMessageResponse.setMe(isMe);
        chatMessageResponse.setCreatedDate(chatMessage.getCreatedDate().toString());
        return chatMessageResponse;
    }
}
