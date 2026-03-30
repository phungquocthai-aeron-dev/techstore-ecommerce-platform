package com.techstore.chat.controller;

import java.time.Instant;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.springframework.stereotype.Component;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.techstore.chat.dto.request.IntrospectRequest;
import com.techstore.chat.dto.response.IntrospectResponse;
import com.techstore.chat.entity.WebSocketSession;
import com.techstore.chat.service.IdentityService;
import com.techstore.chat.service.WebSocketSessionService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SocketHandler {
    SocketIOServer server;
    IdentityService identityService;
    WebSocketSessionService webSocketSessionService;

    //    @OnConnect
    //    public void clientConnected(SocketIOClient client) {
    //        // Get Token from request param
    //        String token = client.getHandshakeData().getSingleUrlParam("token");
    //        String userType = client.getHandshakeData().getSingleUrlParam("userType");
    //
    //        // Verify token
    //        var introspectResponse = identityService.introspect(
    //                IntrospectRequest.builder().token(token).build());
    //
    //        // If Token is invalid disconnect
    //        if (introspectResponse.isValid()) {
    //            log.info("Client connected: {}", client.getSessionId());
    //            // Persist webSocketSession
    //            WebSocketSession webSocketSession = WebSocketSession.builder()
    //                    .socketSessionId(client.getSessionId().toString())
    //                    .userId(introspectResponse.getUserID())
    //                    .userType(userType)
    //                    .createdAt(Instant.now())
    //                    .build();
    //            webSocketSession = webSocketSessionService.create(webSocketSession);
    //
    //            log.info("WebSocketSession created with id: {}", webSocketSession.getId());
    //        } else {
    //            log.error("Authentication fail: {}", client.getSessionId());
    //            client.disconnect();
    //        }
    //    }

    //    @OnConnect
    //    public void clientConnected(SocketIOClient client) {
    //        String token = client.getHandshakeData().getSingleUrlParam("token");
    //        String userType = client.getHandshakeData().getSingleUrlParam("userType");
    //
    //        log.info("client connected");
    //        log.info(token);
    //        log.info(userType);
    //        var introspectResponse = identityService.introspect(
    //                IntrospectRequest.builder().token(token).build());
    //
    //        if (introspectResponse.isValid()) {
    //
    //            String userId = introspectResponse.getUserID();
    //
    //            // 🔥 FIX QUAN TRỌNG
    //            String room = userId + "_" + userType;
    //            client.joinRoom(room);
    //
    //            log.info("Client connected: {}", client.getSessionId());
    //            log.info("JOIN ROOM: {}", room);
    //            log.info("ALL ROOMS: {}", client.getAllRooms());
    //            WebSocketSession webSocketSession = WebSocketSession.builder()
    //                    .socketSessionId(client.getSessionId().toString())
    //                    .userId(userId)
    //                    .userType(userType)
    //                    .createdAt(Instant.now())
    //                    .build();
    //
    //            webSocketSessionService.create(webSocketSession);
    //
    //        } else {
    //            client.disconnect();
    //        }
    //    }

    @OnConnect
    public void clientConnected(SocketIOClient client) {

        // Get Token from request param
        String token = client.getHandshakeData().getSingleUrlParam("token");
        String userType = client.getHandshakeData().getSingleUrlParam("userType");

        if (token == null || userType == null) {
            log.error("No Recieves Params");
            client.disconnect();
        }

        log.info("Recieve Params");
        log.info(token);
        log.info(userType);

        IntrospectResponse introspectResponse = identityService.introspect(
                IntrospectRequest.builder().token(token).build());

        //        // Verify token
        //        var introspectResponse = identityService.introspect(IntrospectRequest.builder()
        //                        .token(token)
        //                .build());

        // If Token is invalid disconnect
        if (introspectResponse.isValid()) {
            log.info("Client connected: {}", client.getSessionId());
            // Persist webSocketSession
            WebSocketSession webSocketSession = WebSocketSession.builder()
                    .socketSessionId(client.getSessionId().toString())
                    .userId(introspectResponse.getUserID())
                    .userType(userType)
                    .createdAt(Instant.now())
                    .build();
            webSocketSession = webSocketSessionService.create(webSocketSession);

            log.info("WebSocketSession created with id: {}", webSocketSession.getId());
        } else {
            log.error("Authentication fail: {}", client.getSessionId());
            client.disconnect();
        }
    }

    @OnDisconnect
    public void clientDisconnected(SocketIOClient client) {
        log.info("Client disConnected: {}", client.getSessionId());
        webSocketSessionService.deleteSession(client.getSessionId().toString());
    }

    @PostConstruct
    public void startServer() {
        server.start();
        server.addListeners(this);
        log.info("Socket server started");
    }

    @PreDestroy
    public void stopServer() {
        server.stop();
        log.info("Socket server stoped");
    }
}
