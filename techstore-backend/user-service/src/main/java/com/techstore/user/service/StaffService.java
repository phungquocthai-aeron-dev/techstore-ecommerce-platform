package com.techstore.user.service;

import java.util.List;
import java.util.Set;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.techstore.user.constant.AccountStatus;
import com.techstore.user.dto.request.StaffRequest;
import com.techstore.user.dto.request.StaffRoleUpdateRequest;
import com.techstore.user.dto.response.StaffResponse;
import com.techstore.user.entity.Role;
import com.techstore.user.entity.Staff;
import com.techstore.user.exception.AppException;
import com.techstore.user.exception.ErrorCode;
import com.techstore.user.mapper.StaffMapper;
import com.techstore.user.repository.RoleRepository;
import com.techstore.user.repository.StaffRepository;
import com.techstore.user.util.PasswordUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository staffRepo;
    private final RoleRepository roleRepo;
    private final StaffMapper staffMapper;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    @PreAuthorize("hasRole('ADMIN')")
    public StaffResponse createStaff(StaffRequest req) {

        if (staffRepo.findByEmail(req.getEmail()).isPresent()) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        Set<Role> roles = roleRepo.findByNameIn(req.getRoleNames());
        if (roles.isEmpty()) {
            throw new AppException(ErrorCode.ROLE_NOT_FOUND);
        }

        String rawPassword = PasswordUtil.randomPassword(8);

        Staff staff = staffMapper.toEntity(req);
        staff.setRoles(roles);
        staff.setStatus(AccountStatus.ACTIVE.name());
        staff.setPassword(passwordEncoder.encode(rawPassword));

        Staff saved = staffRepo.save(staff);

        mailService.sendStaffAccount(saved.getEmail(), rawPassword);

        return staffMapper.toResponse(saved);
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public StaffResponse updateInfo(Long id, StaffRequest req) {
        Staff staff = getStaffAndCheckPermission(id);

        staff.setFullName(req.getFullName());
        staff.setPhone(req.getPhone());

        return staffMapper.toResponse(staffRepo.save(staff));
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public void updatePassword(Long id, String oldPw, String newPw, String passwordConfirm) {
        Staff staff = getStaffAndCheckPermission(id);

        if (!isAdmin() && !passwordEncoder.matches(oldPw, staff.getPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        if (newPw == null || newPw.trim().isEmpty()) {
            throw new AppException(ErrorCode.PASSWORD_EMPTY);
        }

        if (!newPw.equals(passwordConfirm)) {
            throw new AppException(ErrorCode.PASSWORD_CONFIRM_NOT_MATCH);
        }

        if (passwordEncoder.matches(newPw, staff.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_DUPLICATE);
        }

        staff.setPassword(passwordEncoder.encode(newPw));
        staffRepo.save(staff);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void updateStatus(Long id, String status) {
        Staff staff = staffRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        staff.setStatus(status);
        staffRepo.save(staff);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public StaffResponse updateRoles(Long staffId, StaffRoleUpdateRequest req) {

        if (req.getRoleNames() == null || req.getRoleNames().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_ROLE);
        }

        if (staffId.equals(getCurrentUserId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        Staff staff = staffRepo.findById(staffId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Set<Role> roles = roleRepo.findByNameIn(req.getRoleNames());
        if (roles.size() != req.getRoleNames().size()) {
            throw new AppException(ErrorCode.ROLE_NOT_FOUND);
        }

        staff.setRoles(roles);
        return staffMapper.toResponse(staffRepo.save(staff));
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public StaffResponse findById(Long id) {
        if (id == null) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        Staff staff = staffRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return staffMapper.toResponse(staff);
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public StaffResponse findOne(Long id, String email, String phone) {
        if (id == null && email == null && phone == null) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        Staff staff = staffRepo
                .findByIdOrEmailOrPhone(id, email, phone)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return staffMapper.toResponse(staff);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<StaffResponse> search(Long id, String email, String phone, String fullName) {

        return staffRepo.search(id, email, phone, fullName).stream()
                .map(staffMapper::toResponse)
                .toList();
    }

    private Staff getStaffAndCheckPermission(Long id) {
        Staff staff = staffRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!isAdmin() && !staff.getId().equals(getCurrentUserId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return staff;
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Jwt jwt = (Jwt) authentication.getPrincipal();
        return Long.valueOf(jwt.getSubject());
    }

    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
