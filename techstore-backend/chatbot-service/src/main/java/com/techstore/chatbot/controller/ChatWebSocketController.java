package com.techstore.chatbot.controller;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.techstore.chatbot.dto.request.ChatRequest;
import com.techstore.chatbot.dto.request.ChatSocketRequest;
import com.techstore.chatbot.dto.response.ChatResponse;
import com.techstore.chatbot.dto.response.ChatSocketResponse;
import com.techstore.chatbot.service.ChatService;
import com.techstore.chatbot.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final ChatService chatService;
    private final JwtUtil jwtUtil;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Client gửi message lên: /app/chat
     * Server push kết quả về: /user/{sessionId}/queue/response
     *
     * Dùng /user/queue thay vì /topic để mỗi user nhận đúng response của mình.
     */
    @MessageMapping("/chat")
    public void handleChat(@Payload ChatSocketRequest socketRequest, Principal principal) {
        String sessionId = socketRequest.getSessionId();
        // Destination để push response về đúng client
        String destination = "/queue/response";

        log.info("[WS] Received message: sessionId={}, msg=\"{}\"", sessionId, socketRequest.getMessage());

        try {
            // Bước 1: Push trạng thái "đang xử lý" ngay lập tức
            sendToSession(principal, sessionId, destination, ChatSocketResponse.typing());

            // Bước 2: Extract userId từ token gửi kèm (vì WS không có Authorization header)
            Long userId = null;
            if (socketRequest.getToken() != null && !socketRequest.getToken().isBlank()) {
                userId = jwtUtil.extractUserIdFromHeader("Bearer " + socketRequest.getToken());
            }

            // Bước 3: Xử lý qua ChatService (tái sử dụng logic sẵn có)
            ChatRequest chatRequest = new ChatRequest();
            chatRequest.setMessage(socketRequest.getMessage());
            chatRequest.setSessionId(sessionId);

            ChatResponse response = chatService.processMessage(chatRequest, userId);

            // Bước 4: Push kết quả về client
            sendToSession(principal, sessionId, destination, ChatSocketResponse.from(response));

        } catch (Exception ex) {
            log.error("[WS] Error processing message: {}", ex.getMessage(), ex);
            sendToSession(
                    principal, sessionId, destination, ChatSocketResponse.error("⚠️ Có lỗi xảy ra, vui lòng thử lại!"));
        }
    }

    /**
     * Ưu tiên gửi theo Principal (user đã xác thực).
     * Fallback về sessionId nếu anonymous.
     */
    private void sendToSession(Principal principal, String sessionId, String destination, ChatSocketResponse response) {
        if (principal != null) {
            messagingTemplate.convertAndSendToUser(principal.getName(), destination, response);
        } else if (sessionId != null && !sessionId.isBlank()) {
            messagingTemplate.convertAndSendToUser(sessionId, destination, response);
        } else {
            log.warn("[WS] Cannot determine destination — no principal or sessionId");
        }
    }
}
