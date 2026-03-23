package com.techstore.review.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.techstore.review.dto.request.ModerationRequest;
import com.techstore.review.dto.response.ApiResponse;
import com.techstore.review.dto.response.ModerationResult;

@FeignClient(name = "moderation-service", url = "${app.services.moderation}")
public interface ModerationClient {

    @PostMapping("/predict")
    ApiResponse<ModerationResult> predict(@RequestBody ModerationRequest request);
}
