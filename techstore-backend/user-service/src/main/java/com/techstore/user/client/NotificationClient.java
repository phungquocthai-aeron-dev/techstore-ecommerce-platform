package com.techstore.user.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.techstore.user.dto.response.ApiResponse;

@FeignClient(name = "notification-service", url = "${app.services.notification}")
public interface NotificationClient {

    @PostMapping("/notifications/otp/verify")
    ApiResponse<Boolean> verifyOtp(@RequestParam String email, @RequestParam String otp);
}
