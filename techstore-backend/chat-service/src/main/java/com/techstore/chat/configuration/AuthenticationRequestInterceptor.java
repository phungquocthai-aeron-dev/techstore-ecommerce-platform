package com.techstore.chat.configuration;

import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthenticationRequestInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (servletRequestAttributes == null) {
            log.warn("No HttpServletRequest (likely WebSocket context)");
            return;
        }

        var request = servletRequestAttributes.getRequest();
        var authHeader = request.getHeader("Authorization");

        log.info("Header: {}", authHeader);

        if (StringUtils.hasText(authHeader)) {
            template.header("Authorization", authHeader);
        }
    }
}
