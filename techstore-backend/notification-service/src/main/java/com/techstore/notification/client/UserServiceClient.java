package com.techstore.notification.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.techstore.notification.configuration.FileFeignConfig;
import com.techstore.notification.dto.response.ApiResponse;
import com.techstore.notification.dto.response.CustomerAuthDTO;
import com.techstore.notification.dto.response.CustomerResponse;
import com.techstore.notification.dto.response.StaffAuthDTO;
import com.techstore.notification.dto.response.StaffResponse;

@FeignClient(name = "user-service", url = "${app.services.user}", configuration = FileFeignConfig.class)
public interface UserServiceClient {

    @GetMapping("/staffs/{staffId}")
    ApiResponse<StaffResponse> getStaffById(@PathVariable Long staffId);

    @GetMapping("/customers/{customerId}")
    ApiResponse<CustomerResponse> getCustomerById(@PathVariable Long customerId);

    @GetMapping("/internal/auth/staff")
    StaffAuthDTO getStaffByEmail(@RequestParam String email);

    @GetMapping("/internal/auth/customer")
    CustomerAuthDTO getCustomerByEmail(@RequestParam String email);

    @PutMapping("/customers/{id}/otp/password")
    ApiResponse<Void> updateCustomerPasswordByOtp(
            @PathVariable Long id, @RequestParam String newPassword, @RequestParam String passwordConfirm);

    @PutMapping("/staffs/{id}/otp/password")
    ApiResponse<Void> updateStaffPasswordByOtp(
            @PathVariable Long id, @RequestParam String newPassword, @RequestParam String passwordConfirm);
}
