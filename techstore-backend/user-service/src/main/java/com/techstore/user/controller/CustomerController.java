package com.techstore.user.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.techstore.user.dto.request.CustomerRegisterRequest;
import com.techstore.user.dto.request.CustomerUpdateRequest;
import com.techstore.user.dto.request.ResetPasswordRequest;
import com.techstore.user.dto.response.ApiResponse;
import com.techstore.user.dto.response.CustomerResponse;
import com.techstore.user.service.CustomerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/register")
    public ApiResponse<CustomerResponse> register(@RequestBody CustomerRegisterRequest req) {

        return ApiResponse.<CustomerResponse>builder()
                .result(customerService.register(req))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<CustomerResponse> getById(@PathVariable Long id) {
        return ApiResponse.<CustomerResponse>builder()
                .result(customerService.getById(id))
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<List<CustomerResponse>> search(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String fullName) {

        return ApiResponse.<List<CustomerResponse>>builder()
                .result(customerService.findCustomer(id, email, phone, fullName))
                .build();
    }

    @GetMapping("/all")
    public ApiResponse<List<CustomerResponse>> getAll() {
        return ApiResponse.<List<CustomerResponse>>builder()
                .result(customerService.getAll())
                .build();
    }

    @GetMapping("/paged")
    public ApiResponse<Page<CustomerResponse>> getAllPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        return ApiResponse.<Page<CustomerResponse>>builder()
                .result(customerService.getAllPaged(page, size, sortBy, sortDir))
                .build();
    }

    @PatchMapping("/{id}")
    public ApiResponse<CustomerResponse> updateInfo(@PathVariable Long id, @RequestBody CustomerUpdateRequest req) {

        return ApiResponse.<CustomerResponse>builder()
                .result(customerService.updateInfo(id, req))
                .build();
    }

    @PostMapping("/{id}/avatar")
    public ApiResponse<Void> uploadAvatar(@PathVariable Long id, @RequestPart MultipartFile file) {

        customerService.updateAvatar(id, file);
        return ApiResponse.<Void>builder().build();
    }

    @PutMapping("/{id}/password")
    public ApiResponse<Void> updatePassword(
            @PathVariable Long id,
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            @RequestParam String passwordConfirm) {

        customerService.updatePassword(id, oldPassword, newPassword, passwordConfirm);
        return ApiResponse.<Void>builder().build();
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {

        customerService.updateStatus(id, status);
        return ApiResponse.<Void>builder().build();
    }

    // Dùng OTP

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@RequestParam String email) {
        customerService.forgotPassword(email);
        return ApiResponse.<Void>builder()
                .message("OTP đã được gửi đến email của bạn")
                .build();
    }

    @PutMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@RequestBody ResetPasswordRequest req) {
        customerService.resetPassword(req);
        return ApiResponse.<Void>builder().message("Đổi mật khẩu thành công").build();
    }
}
