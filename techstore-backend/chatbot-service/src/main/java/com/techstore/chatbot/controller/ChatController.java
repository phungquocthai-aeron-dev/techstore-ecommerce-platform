package com.techstore.chatbot.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.techstore.chatbot.dto.request.ChatRequest;
import com.techstore.chatbot.dto.response.ApiResponse;
import com.techstore.chatbot.dto.response.ChatResponse;
import com.techstore.chatbot.service.ChatService;
import com.techstore.chatbot.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<ApiResponse<ChatResponse>> chat(
            @Valid @RequestBody ChatRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        // Extract userId từ JWT nếu có
        Long userId = jwtUtil.extractUserIdFromHeader(authHeader);

        log.info("Chat request: userId={}, sessionId={}", userId, request.getSessionId());

        ChatResponse response = chatService.processMessage(request, userId);

        return ResponseEntity.ok(
                ApiResponse.<ChatResponse>builder().result(response).build());
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Object>> getChatHistory(
            @RequestParam(required = false) String sessionId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = jwtUtil.extractUserIdFromHeader(authHeader);

        Object history = chatService.getChatHistory(userId, sessionId);

        return ResponseEntity.ok(ApiResponse.builder().result(history).build());
    }
}
