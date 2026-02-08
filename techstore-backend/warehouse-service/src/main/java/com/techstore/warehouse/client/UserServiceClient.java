package com.techstore.warehouse.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.techstore.warehouse.dto.response.ApiResponse;

@FeignClient(name = "user-service", url = "${user.service.url:http://localhost:8081}")
public interface UserServiceClient {

    @GetMapping("/staff/{staffId}")
    ApiResponse<Object> getStaffById(@PathVariable Long staffId);
}
