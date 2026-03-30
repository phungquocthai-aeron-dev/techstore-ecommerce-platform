package com.techstore.user.service;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.techstore.event.dto.NotificationEvent;
import com.techstore.user.client.NotificationClient;
import com.techstore.user.constant.AccountStatus;
import com.techstore.user.dto.request.ResetPasswordRequest;
import com.techstore.user.dto.request.StaffRequest;
import com.techstore.user.dto.request.StaffRoleUpdateRequest;
import com.techstore.user.dto.response.ApiResponse;
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
    private final NotificationClient notificationClient;
    private final OtpEventProducer otpEventProducer;
    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

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

        String htmlBody = buildStaffAccountEmail(saved.getFullName(), saved.getEmail(), rawPassword);

        NotificationEvent event = NotificationEvent.builder()
                .channel("EMAIL")
                .recipient(saved.getEmail())
                .subject("Tài khoản nhân viên TechStore")
                .body(htmlBody)
                .build();

        kafkaTemplate.send("notification-delivery", event);

        return staffMapper.toResponse(saved);
    }

    @PreAuthorize("hasAnyRole('ADMIN','SALES_STAFF', 'WAREHOUSE_STAFF')")
    public StaffResponse updateInfo(Long id, StaffRequest req) {
        Staff staff = getStaffAndCheckPermission(id);

        staff.setFullName(req.getFullName());
        staff.setPhone(req.getPhone());

        return staffMapper.toResponse(staffRepo.save(staff));
    }

    @PreAuthorize("hasAnyRole('ADMIN','SALES_STAFF', 'WAREHOUSE_STAFF')")
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
    public Page<StaffResponse> getAllPaged(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return staffRepo.findAll(pageable).map(staffMapper::toResponse);
    }

    public void forgotPassword(String email) {
        Staff staff = staffRepo.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!AccountStatus.ACTIVE.name().equals(staff.getStatus())) {
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        }

        otpEventProducer.sendOtpEvent(email, "STAFF");
    }

    public void resetPasswordByOtp(ResetPasswordRequest req) {
        if (!req.getNewPassword().equals(req.getPasswordConfirm())) {
            throw new AppException(ErrorCode.PASSWORD_CONFIRM_NOT_MATCH);
        }

        ApiResponse<Boolean> response = notificationClient.verifyOtp(req.getEmail(), req.getOtp());

        if (response.getResult() == null || !response.getResult()) {
            throw new AppException(ErrorCode.INVALID_OTP);
        }

        Staff staff =
                staffRepo.findByEmail(req.getEmail()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (passwordEncoder.matches(req.getNewPassword(), staff.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_DUPLICATE);
        }

        staff.setPassword(passwordEncoder.encode(req.getNewPassword()));
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

    public StaffResponse findById(Long id) {
        if (id == null) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        Staff staff = staffRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return staffMapper.toResponse(staff);
    }

    public List<StaffResponse> getByIds(List<Long> ids) {

        if (ids == null || ids.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        return staffRepo.findAllById(ids).stream().map(staffMapper::toResponse).toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN','SALES_STAFF', 'WAREHOUSE_STAFF')")
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

    @PreAuthorize("hasRole('ADMIN')")
    public List<StaffResponse> getByRole(String roleName) {

        if (roleName == null || roleName.trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        List<Staff> staffs = staffRepo.findByRoleName(roleName);

        return staffs.stream().map(staffMapper::toResponse).toList();
    }

    public List<StaffResponse> getChatAvailableStaff() {

        Set<String> roles = Set.of("ADMIN", "SALES_STAFF");

        List<Staff> staffs = staffRepo.findByRoleNames(roles);

        return staffs.stream().map(staffMapper::toResponse).toList();
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

    private String buildStaffAccountEmail(String name, String email, String password) {
        return """
			<div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px;">

				<h2 style="color: #2c3e50;">Chào %s 👋</h2>

				<p>Tài khoản nhân viên của bạn đã được tạo thành công.</p>

				<div style="background: #f5f5f5; padding: 15px; border-radius: 8px; margin: 20px 0;">
					<p><strong>Email:</strong> %s</p>
					<p><strong>Mật khẩu:</strong> %s</p>
				</div>

				<p style="color: #e74c3c;">
					⚠️ Vui lòng đăng nhập và đổi mật khẩu ngay để đảm bảo an toàn.
				</p>

				<a href="http://localhost:4300/auth"
				style="display:inline-block; padding:10px 20px; background:#4CAF50;
						color:white; text-decoration:none; border-radius:5px;">
					Đăng nhập ngay
				</a>

				<br/><br/>
				<p style="color: #888;">TechStore Team</p>
			</div>
		"""
                .formatted(name, email, password);
    }
}
