package com.techstore.chat.service;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.techstore.chat.client.IdentityClient;
import com.techstore.chat.dto.request.IntrospectRequest;
import com.techstore.chat.dto.response.IntrospectResponse;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IdentityService {
    IdentityClient identityClient;

    public IntrospectResponse introspect(IntrospectRequest request) {
        try {
            log.info("Token gửi đi: " + request.getToken());
            var result = identityClient.introspect(request).getResult();
            if (Objects.isNull(result)) {
                log.info("Authen null");
                return IntrospectResponse.builder().valid(false).build();
            }
            log.info("Authen");
            log.info(result.getUserID());
            log.info("Check: " + result.isValid());
            return result;
        } catch (Exception e) {
            log.error("Introspect failed: {}", e.getMessage(), e);
            return IntrospectResponse.builder().valid(false).build();
        }
    }
}
