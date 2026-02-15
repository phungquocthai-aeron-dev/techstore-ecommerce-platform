package com.techstore.warehouse.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.techstore.warehouse.configuration.FileFeignConfig;
import com.techstore.warehouse.dto.response.ApiResponse;
import com.techstore.warehouse.dto.response.StaffResponse;

@FeignClient(name = "user-service", url = "${app.services.user}", configuration = FileFeignConfig.class)
public interface UserServiceClient {

    @GetMapping("/staffs/{staffId}")
    ApiResponse<StaffResponse> getStaffById(@PathVariable Long staffId);
}
