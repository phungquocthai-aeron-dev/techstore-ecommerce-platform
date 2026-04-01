package com.techstore.chatbot.service;

import java.time.Instant;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.techstore.chatbot.dto.response.SessionResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SessionService {

    @Value("${app.session.secret}")
    private String sessionSecret;

    @Value("${app.session.ttl-hours:24}")
    private long ttlHours;

    /**
     * Tạo sessionId mới + ký HMAC để client không thể giả mạo.
     */
    public SessionResponse createSession() {
        String sessionId = UUID.randomUUID().toString();
        long expiresAt = Instant.now().plusSeconds(ttlHours * 3600).getEpochSecond();

        String sessionToken = sign(sessionId, expiresAt);

        log.info("[Session] Created sessionId={}, expiresAt={}", sessionId, expiresAt);

        return SessionResponse.builder()
                .sessionId(sessionId)
                .sessionToken(sessionToken) // gửi về client, client gửi lại mỗi request
                .expiresAt(expiresAt)
                .build();
    }

    /**
     * Verify sessionToken mà client gửi lên.
     * Trả về sessionId nếu hợp lệ, null nếu không.
     */
    public String verifySessionToken(String sessionToken) {
        if (sessionToken == null || sessionToken.isBlank()) return null;

        try {
            // Format: sessionId.expiresAt.signature
            String[] parts = sessionToken.split("\\.");
            if (parts.length != 3) return null;

            String sessionId = parts[0];
            long expiresAt = Long.parseLong(parts[1]);

            // Kiểm tra hết hạn
            if (Instant.now().getEpochSecond() > expiresAt) {
                log.warn("[Session] Token expired: sessionId={}", sessionId);
                return null;
            }

            // Kiểm tra chữ ký
            String expectedToken = sign(sessionId, expiresAt);
            if (!expectedToken.equals(sessionToken)) {
                log.warn("[Session] Invalid signature: sessionId={}", sessionId);
                return null;
            }

            return sessionId;

        } catch (Exception ex) {
            log.warn("[Session] Verify failed: {}", ex.getMessage());
            return null;
        }
    }

    /**
     * Ký HMAC-SHA256: sessionId.expiresAt.signature
     */
    private String sign(String sessionId, long expiresAt) {
        try {
            String payload = sessionId + "." + expiresAt;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(sessionSecret.getBytes(), "HmacSHA256"));
            byte[] raw = mac.doFinal(payload.getBytes());

            // Encode base64 url-safe, bỏ padding
            String signature = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(raw);

            return payload + "." + signature;

        } catch (Exception ex) {
            throw new RuntimeException("Failed to sign session", ex);
        }
    }
}
