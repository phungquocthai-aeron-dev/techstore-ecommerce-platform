package com.techstore.user.controller.internal;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.techstore.user.dto.request.GoogleAuthRequest;
import com.techstore.user.dto.response.ApiResponse;
import com.techstore.user.dto.response.CustomerAuthResponse;
import com.techstore.user.dto.response.CustomerResponse;
import com.techstore.user.dto.response.StaffAuthResponse;
import com.techstore.user.dto.response.StaffResponse;
import com.techstore.user.service.CustomerService;
import com.techstore.user.service.StaffService;
import com.techstore.user.service.UserInternalService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/internal/auth")
@RequiredArgsConstructor
public class UserInternalController {

    private final UserInternalService userInternalService;
    private final CustomerService customerService;
    private final StaffService staffService;

    @GetMapping("/staff")
    public StaffAuthResponse getStaff(@RequestParam String email) {
        return userInternalService.getStaffForAuth(email);
    }

    @GetMapping("/customer")
    public CustomerAuthResponse getCustomer(@RequestParam String email) {
        return userInternalService.getCustomerForAuth(email);
    }

    @GetMapping("/staff/{id}")
    public StaffAuthResponse getStaffById(@PathVariable Long id) {
        return userInternalService.getStaffForAuth(id);
    }

    @GetMapping("/customer/{id}")
    public CustomerAuthResponse getCustomerById(@PathVariable Long id) {
        return userInternalService.getCustomerForAuth(id);
    }

    @PostMapping("/customer/google")
    public CustomerAuthResponse handleGoogle(@RequestBody GoogleAuthRequest request) {
        return userInternalService.handleGoogleCustomer(request);
    }

    @GetMapping("/customers/ids")
    public ApiResponse<List<CustomerResponse>> geCustomertByIds(@RequestParam List<Long> ids) {
        return ApiResponse.<List<CustomerResponse>>builder()
                .result(customerService.getByIds(ids))
                .build();
    }

    @GetMapping("/staffs/ids")
    public ApiResponse<List<StaffResponse>> getStaffByIds(@RequestParam List<Long> ids) {
        return ApiResponse.<List<StaffResponse>>builder()
                .result(staffService.getByIds(ids))
                .build();
    }
}
