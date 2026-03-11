package com.techstore.user.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.techstore.user.client.FileServiceClient;
import com.techstore.user.constant.AccountStatus;
import com.techstore.user.constant.UploadFolder;
import com.techstore.user.dto.request.CustomerRegisterRequest;
import com.techstore.user.dto.request.CustomerUpdateRequest;
import com.techstore.user.dto.response.CustomerResponse;
import com.techstore.user.entity.Customer;
import com.techstore.user.exception.AppException;
import com.techstore.user.exception.ErrorCode;
import com.techstore.user.mapper.CustomerMapper;
import com.techstore.user.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepo;
    private final PasswordEncoder passwordEncoder;
    private final FileServiceClient fileClient;
    private final CustomerMapper customerMapper;

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public List<CustomerResponse> findCustomer(Long id, String email, String phone, String fullName) {
        if (id == null && email == null && phone == null && fullName == null) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        List<Customer> customers = customerRepo.findByIdOrEmailOrPhone(id, email, phone, fullName);

        return customers.stream().map(customerMapper::toResponse).toList();
    }

    public CustomerResponse getById(Long id) {
        return customerMapper.toResponse(
                customerRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public CustomerResponse getByEmail(String email) {
        return customerMapper.toResponse(
                customerRepo.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public CustomerResponse getByPhone(String phone) {
        return customerMapper.toResponse(
                customerRepo.findByPhone(phone).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public List<CustomerResponse> getAll() {
        return customerRepo.findAll().stream().map(customerMapper::toResponse).toList();
    }

    public CustomerResponse register(CustomerRegisterRequest req) {
        if (customerRepo.findByEmail(req.getEmail()).isPresent()) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        if (!req.getPassword().equals(req.getPasswordConfirm())) {
            throw new AppException(ErrorCode.PASSWORD_CONFIRM_NOT_MATCH);
        }

        Customer customer = customerMapper.toEntity(req);
        customer.setPassword(passwordEncoder.encode(req.getPassword()));
        customer.setStatus(AccountStatus.ACTIVE.name());

        return customerMapper.toResponse(customerRepo.save(customer));
    }

    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public CustomerResponse updateInfo(Long id, CustomerUpdateRequest req) {
        Customer customer = getCustomerAndCheckPermission(id);

        customerMapper.updateEntityFromRequest(req, customer);
        return customerMapper.toResponse(customerRepo.save(customer));
    }

    public List<CustomerResponse> getByIds(List<Long> ids) {

        if (ids == null || ids.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        return customerRepo.findAllById(ids).stream()
                .map(customerMapper::toResponse)
                .toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public void updateAvatar(Long id, MultipartFile file) {
        Customer customer = getCustomerAndCheckPermission(id);

        var upload = fileClient.upload(file, UploadFolder.USER_AVATAR.name());
        customer.setAvatarUrl(upload.getResult().getUrl());

        customerRepo.save(customer);
    }

    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public void updatePassword(Long id, String oldPw, String newPw, String passwordConfirm) {
        Customer customer = getCustomerAndCheckPermission(id);

        if (!isAdmin() && !passwordEncoder.matches(oldPw, customer.getPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        if (newPw == null || newPw.trim().isEmpty()) {
            throw new AppException(ErrorCode.PASSWORD_EMPTY);
        }

        if (!newPw.equals(passwordConfirm)) {
            throw new AppException(ErrorCode.PASSWORD_CONFIRM_NOT_MATCH);
        }

        if (passwordEncoder.matches(newPw, customer.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_DUPLICATE);
        }

        customer.setPassword(passwordEncoder.encode(newPw));
        customerRepo.save(customer);
    }

    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public void updateStatus(Long id, String newStatus) {
        Customer customer = customerRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (isAdmin()) {
            customer.setStatus(newStatus);
            customerRepo.save(customer);
            return;
        }

        if (!customer.getId().equals(getCurrentUserId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (customer.getStatus() == AccountStatus.ACTIVE.name() && newStatus == AccountStatus.DISABLED.name()) {

            customer.setStatus(AccountStatus.DISABLED.name());
            customerRepo.save(customer);
            return;
        }

        throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    private Customer getCustomerAndCheckPermission(Long id) {
        Customer customer = customerRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!isAdmin() && !customer.getId().equals(getCurrentUserId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return customer;
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
