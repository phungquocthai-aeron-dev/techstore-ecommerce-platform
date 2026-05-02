package com.techstore.cart.presentation;

import com.techstore.cart.infrastructure.config.exception.AppException;
import com.techstore.cart.infrastructure.config.exception.ErrorCode;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * SecurityContextHelper - Extracts authenticated customer info from the JWT in SecurityContext.
 * Kept in the presentation layer — only controllers/handlers need this.
 */
@Component
public class SecurityContextHelper {

    public Long getAuthenticatedCustomerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        Jwt jwt = jwtAuth.getToken();
        String userType = jwt.getClaim("user_type");

        if (!"CUSTOMER".equals(userType)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return Long.valueOf(jwt.getSubject());
    }
}
