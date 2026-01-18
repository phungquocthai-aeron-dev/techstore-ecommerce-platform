package com.techstore.identity.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.techstore.identity.client.dto.CustomerAuthDTO;
import com.techstore.identity.client.dto.GoogleAuthDTO;
import com.techstore.identity.client.dto.StaffAuthDTO;
import com.techstore.identity.configuration.UserFeignConfig;

@FeignClient(name = "user-service", url = "${app.services.user}", configuration = UserFeignConfig.class)
public interface UserServiceClient {
    @GetMapping("/internal/auth/staff")
    StaffAuthDTO getStaffByEmail(@RequestParam String email);

    @GetMapping("/internal/auth/staff/{id}")
    StaffAuthDTO getStaffById(@PathVariable Long id);

    @GetMapping("/internal/auth/customer")
    CustomerAuthDTO getCustomerByEmail(@RequestParam String email);

    @GetMapping("/internal/auth/customer/{id}")
    CustomerAuthDTO getCustomerById(@PathVariable Long id);

    @PostMapping("/internal/auth/customer/google")
    CustomerAuthDTO handleGoogle(@RequestBody GoogleAuthDTO request);
}
