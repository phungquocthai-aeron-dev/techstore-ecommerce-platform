package com.techstore.identity.controller;

import java.text.ParseException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nimbusds.jose.JOSEException;
import com.techstore.identity.dto.request.AuthenticationRequest;
import com.techstore.identity.dto.request.IntrospectRequest;
import com.techstore.identity.dto.request.LogoutRequest;
import com.techstore.identity.dto.request.RefreshRequest;
import com.techstore.identity.dto.response.ApiResponse;
import com.techstore.identity.dto.response.AuthenticationResponse;
import com.techstore.identity.dto.response.IntrospectResponse;
import com.techstore.identity.service.AuthenticationService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;

    @PostMapping("/token/staff")
    ApiResponse<AuthenticationResponse> authenticateStaff(@RequestBody AuthenticationRequest request) {
        System.out.println(request.getPassword());
        System.out.println(request.getUsername());
        var result = authenticationService.authenticateStaff(request);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/token/customer")
    ApiResponse<AuthenticationResponse> authenticateCustomer(@RequestBody AuthenticationRequest request) {
        var result = authenticationService.authenticateCustomer(request);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request) {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder().result(result).build();
    }

    @PostMapping("/refresh/staff")
    ApiResponse<AuthenticationResponse> authenticateStaff(@RequestBody RefreshRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.refreshStaffToken(request);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/refresh/customer")
    ApiResponse<AuthenticationResponse> authenticateCustomer(@RequestBody RefreshRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.refreshCustomerToken(request);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder().build();
    }
}
