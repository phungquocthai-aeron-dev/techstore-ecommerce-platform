package com.techstore.chat.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.techstore.chat.configuration.FileFeignConfig;
import com.techstore.chat.dto.response.ApiResponse;
import com.techstore.chat.dto.response.CustomerResponse;
import com.techstore.chat.dto.response.StaffResponse;

@FeignClient(name = "user-service", url = "${app.services.user}", configuration = FileFeignConfig.class)
public interface UserServiceClient {

    @GetMapping("/staffs/{staffId}")
    ApiResponse<StaffResponse> getStaffById(@PathVariable Long staffId);

    @GetMapping("/customers/{customerId}")
    ApiResponse<CustomerResponse> getCustomerById(@PathVariable Long customerId);
}
