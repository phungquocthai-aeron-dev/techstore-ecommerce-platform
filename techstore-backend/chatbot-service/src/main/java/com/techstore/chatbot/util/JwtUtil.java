// package com.techstore.chatbot.util;
//
// import org.springframework.stereotype.Component;
//
// import com.techstore.chatbot.client.IdentityClient;
// import com.techstore.chatbot.dto.request.IntrospectRequest;
// import com.techstore.chatbot.dto.response.IntrospectResponse;
//
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
//
// @Component
// @RequiredArgsConstructor
// @Slf4j
// public class JwtUtil {
//
//    private final IdentityClient identityClient;
//
//    /**
//     * Extract userId từ Authorization header.
//     * Gọi identity-service để introspect token.
//     *
//     * @param authHeader "Bearer <token>" hoặc null
//     * @return userId nếu token hợp lệ, null nếu không
//     */
//    public Long extractUserIdFromHeader(String authHeader) {
//        String token = extractToken(authHeader);
//        if (token == null) return null;
//
//        try {
//            IntrospectResponse response = identityClient
//                    .introspect(IntrospectRequest.builder().token(token).build())
//                    .getResult();
//
//            if (response == null || !response.isValid()) {
//                log.debug("[JWT] Token invalid or expired");
//                return null;
//            }
//
//            String userId = response.getUserID();
//            if (userId == null || userId.isBlank()) {
//                log.warn("[JWT] Token valid but userId is blank");
//                return null;
//            }
//
//            return Long.parseLong(userId);
//
//        } catch (NumberFormatException ex) {
//            log.warn("[JWT] userId is not a Long: {}", ex.getMessage());
//            return null;
//        } catch (Exception ex) {
//            log.warn("[JWT] Introspect failed: {}", ex.getMessage());
//            return null;
//        }
//    }
//
//    /**
//     * Strip "Bearer " prefix từ Authorization header.
//     */
//    private String extractToken(String authHeader) {
//        if (authHeader == null || authHeader.isBlank()) return null;
//        if (authHeader.startsWith("Bearer ")) return authHeader.substring(7).trim();
//        return authHeader.trim();
//    }
// }
package com.techstore.chatbot.util;

import java.text.ParseException;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.signerKey}")
    private String SIGNER_KEY;

    public Long extractUserIdFromHeader(String authHeader) {
        String token = extractToken(authHeader);
        if (token == null) return null;

        try {
            SignedJWT jwt = SignedJWT.parse(token);

            if (!jwt.verify(new MACVerifier(SIGNER_KEY.getBytes()))) {
                return null;
            }

            Date exp = jwt.getJWTClaimsSet().getExpirationTime();
            if (exp.before(new Date())) {
                return null;
            }

            String userId = jwt.getJWTClaimsSet().getSubject();
            return userId != null ? Long.parseLong(userId) : null;

        } catch (ParseException | JOSEException e) {
            log.warn("[JWT] Decode failed: {}", e.getMessage());
            return null;
        }
    }

    private String extractToken(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) return null;
        if (authHeader.startsWith("Bearer ")) return authHeader.substring(7);
        return authHeader;
    }
}
