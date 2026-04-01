package com.techstore.chatbot.configuration;

import java.security.Principal;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.techstore.chatbot.service.SessionService;
import com.techstore.chatbot.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final SessionService sessionService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) return message;

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            handleConnect(accessor);
        }

        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {

        String authHeader = accessor.getFirstNativeHeader("Authorization");

        // 👉 Nếu có token → bắt buộc phải valid
        if (authHeader != null) {
            Long userId = jwtUtil.extractUserIdFromHeader(authHeader);

            if (userId == null) {
                throw new AccessDeniedException("Invalid JWT");
            }

            setPrincipal(accessor, userId.toString());
            log.info("[WS] Authenticated userId={}", userId);
            return;
        }

        // 👉 Không có token → dùng session anonymous
        String sessionToken = accessor.getFirstNativeHeader("sessionToken");
        String sessionId = sessionService.verifySessionToken(sessionToken);

        if (sessionId != null) {
            setPrincipal(accessor, sessionId);
            log.info("[WS] Anonymous sessionId={}", sessionId);
            return;
        }

        throw new AccessDeniedException("No auth provided");
    }

    private void setPrincipal(StompHeaderAccessor accessor, String name) {
        accessor.setUser(new Principal() {
            @Override
            public String getName() {
                return name;
            }
        });
    }
}
