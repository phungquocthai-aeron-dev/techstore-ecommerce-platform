package com.techstore.chatbot.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.techstore.chatbot.dto.request.IntrospectRequest;
import com.techstore.chatbot.dto.response.ApiResponse;
import com.techstore.chatbot.dto.response.IntrospectResponse;

@FeignClient(name = "identity-service", url = "${app.services.identity}")
public interface IdentityClient {

    @PostMapping("/auth/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request);
}
