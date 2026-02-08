package com.techstore.user.controller.internal;

import org.springframework.web.bind.annotation.*;

import com.techstore.user.dto.request.GoogleAuthRequest;
import com.techstore.user.dto.response.CustomerAuthResponse;
import com.techstore.user.dto.response.StaffAuthResponse;
import com.techstore.user.service.UserInternalService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/internal/auth")
@RequiredArgsConstructor
public class UserInternalController {

    private final UserInternalService userInternalService;

    @GetMapping("/staff")
    public StaffAuthResponse getStaff(@RequestParam String email) {
        System.err.println("AAAAAAA");
        return userInternalService.getStaffForAuth(email);
    }

    @GetMapping("/customer")
    public CustomerAuthResponse getCustomer(@RequestParam String email) {
        return userInternalService.getCustomerForAuth(email);
    }

    @PostMapping("/customer/google")
    public CustomerAuthResponse handleGoogle(@RequestBody GoogleAuthRequest request) {
        return userInternalService.handleGoogleCustomer(request);
    }
}
