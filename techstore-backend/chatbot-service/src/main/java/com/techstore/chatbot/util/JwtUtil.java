package com.techstore.chatbot.util;

import java.util.Base64;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility đơn giản để đọc thông tin từ JWT token.
 *
 * <p>Không dùng Spring Security — chỉ parse payload của JWT để lấy userId (subject).
 * Token được verify bởi API Gateway trước khi đến service này.
 *
 * <p>JWT format: header.payload.signature (Base64 encoded)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUtil {

    private final ObjectMapper objectMapper;

    /**
     * Extract userId từ Authorization header.
     *
     * @param authHeader giá trị header "Authorization: Bearer {token}"
     * @return userId (Long) nếu parse được, null nếu không có hoặc lỗi
     */
    public Long extractUserIdFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7); // Bỏ "Bearer "
        return extractSubject(token);
    }

    /**
     * Parse payload của JWT và đọc claim "sub" (subject = userId).
     */
    private Long extractSubject(String token) {
        try {
            // JWT gồm 3 phần cách nhau bằng dấu "."
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;

            // Decode phần payload (phần thứ 2)
            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            String payloadJson = new String(payloadBytes);

            JsonNode payload = objectMapper.readTree(payloadJson);
            JsonNode subNode = payload.get("sub");

            if (subNode == null || subNode.isNull()) return null;

            return Long.valueOf(subNode.asText());

        } catch (Exception ex) {
            log.debug("Failed to parse JWT: {}", ex.getMessage());
            return null;
        }
    }
}
