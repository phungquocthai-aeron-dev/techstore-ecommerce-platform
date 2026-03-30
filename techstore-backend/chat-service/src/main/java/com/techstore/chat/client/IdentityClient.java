package com.techstore.chat.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.techstore.chat.dto.request.IntrospectRequest;
import com.techstore.chat.dto.response.ApiResponse;
import com.techstore.chat.dto.response.IntrospectResponse;

@FeignClient(name = "identity-service", url = "${app.services.identity}")
public interface IdentityClient {

    @PostMapping("/auth/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request);
}
