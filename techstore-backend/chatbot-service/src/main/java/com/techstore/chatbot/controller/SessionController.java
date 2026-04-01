package com.techstore.chatbot.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techstore.chatbot.dto.response.ApiResponse;
import com.techstore.chatbot.dto.response.SessionResponse;
import com.techstore.chatbot.service.SessionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/session")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    /**
     * Client gọi 1 lần khi mở app để lấy sessionId.
     * Không cần auth — dành cho anonymous user.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SessionResponse>> createSession() {
        SessionResponse session = sessionService.createSession();
        return ResponseEntity.ok(
                ApiResponse.<SessionResponse>builder().result(session).build());
    }
}
