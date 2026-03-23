package com.techstore.user.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.techstore.user.dto.request.ResetPasswordRequest;
import com.techstore.user.dto.request.StaffRequest;
import com.techstore.user.dto.request.StaffRoleUpdateRequest;
import com.techstore.user.dto.response.ApiResponse;
import com.techstore.user.dto.response.StaffResponse;
import com.techstore.user.service.StaffService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/staffs")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @PostMapping
    public ApiResponse<StaffResponse> create(@Valid @RequestBody StaffRequest req) {
        return ApiResponse.<StaffResponse>builder()
                .result(staffService.createStaff(req))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<StaffResponse> findById(@PathVariable Long id) {
        return ApiResponse.<StaffResponse>builder()
                .result(staffService.findById(id))
                .build();
    }

    @GetMapping
    public ApiResponse<StaffResponse> findOne(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone) {

        return ApiResponse.<StaffResponse>builder()
                .result(staffService.findOne(id, email, phone))
                .build();
    }

    @GetMapping("/paged")
    public ApiResponse<Page<StaffResponse>> getAllPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        return ApiResponse.<Page<StaffResponse>>builder()
                .result(staffService.getAllPaged(page, size, sortBy, sortDir))
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<List<StaffResponse>> search(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String fullName) {

        return ApiResponse.<List<StaffResponse>>builder()
                .result(staffService.search(id, email, phone, fullName))
                .build();
    }

    @PatchMapping("/{id}")
    public ApiResponse<StaffResponse> updateInfo(@PathVariable Long id, @Valid @RequestBody StaffRequest req) {

        return ApiResponse.<StaffResponse>builder()
                .result(staffService.updateInfo(id, req))
                .build();
    }

    @PutMapping("/{id}/password")
    public ApiResponse<Void> updatePassword(
            @PathVariable Long id,
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            @RequestParam String passwordConfirm) {

        staffService.updatePassword(id, oldPassword, newPassword, passwordConfirm);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@RequestParam String email) {
        staffService.forgotPassword(email);
        return ApiResponse.<Void>builder()
                .message("OTP đã được gửi đến email của bạn")
                .build();
    }

    @PutMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@RequestBody ResetPasswordRequest req) {
        staffService.resetPasswordByOtp(req);
        return ApiResponse.<Void>builder().message("Đổi mật khẩu thành công").build();
    }

    @PutMapping("/{id}/roles")
    public ApiResponse<StaffResponse> updateRoles(@PathVariable Long id, @RequestBody StaffRoleUpdateRequest req) {

        return ApiResponse.<StaffResponse>builder()
                .result(staffService.updateRoles(id, req))
                .build();
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {

        staffService.updateStatus(id, status);
        return ApiResponse.<Void>builder().build();
    }
}
