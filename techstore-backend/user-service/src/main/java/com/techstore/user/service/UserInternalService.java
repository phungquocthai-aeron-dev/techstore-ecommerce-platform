package com.techstore.user.service;

import org.springframework.stereotype.Service;

import com.techstore.user.dto.request.GoogleAuthRequest;
import com.techstore.user.dto.response.CustomerAuthResponse;
import com.techstore.user.dto.response.StaffAuthResponse;
import com.techstore.user.entity.Customer;
import com.techstore.user.entity.Staff;
import com.techstore.user.repository.CustomerRepository;
import com.techstore.user.repository.StaffRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserInternalService {

    private final StaffRepository staffRepository;
    private final CustomerRepository customerRepository;

    public StaffAuthResponse getStaffForAuth(String email) {
        Staff staff = staffRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("STAFF_NOT_FOUND"));

        String scope = staff.getRoles().stream()
                .map(role -> "ROLE_" + role.getName())
                .reduce((a, b) -> a + " " + b)
                .orElse("");

        return StaffAuthResponse.builder()
                .id(staff.getId())
                .email(staff.getEmail())
                .password(staff.getPassword())
                .status(staff.getStatus())
                .scope(scope)
                .build();
    }

    public CustomerAuthResponse getCustomerForAuth(String email) {
        Customer customer =
                customerRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("CUSTOMER_NOT_FOUND"));

        return CustomerAuthResponse.builder()
                .id(customer.getId())
                .email(customer.getEmail())
                .password(customer.getPassword())
                .status(customer.getStatus())
                .build();
    }

    public CustomerAuthResponse handleGoogleCustomer(GoogleAuthRequest request) {

        Customer customer = customerRepository.findByEmail(request.getEmail()).orElse(null);

        if (customer == null) {
            customer = Customer.builder()
                    .email(request.getEmail())
                    .fullName(request.getFullName())
                    .avatarUrl(request.getAvatarUrl())
                    .provider("GOOGLE")
                    .providerId(request.getProviderId())
                    .status("ACTIVE")
                    .build();

            customerRepository.save(customer);
        } else {
            if (customer.getProvider() != null && !"GOOGLE".equals(customer.getProvider())) {
                throw new RuntimeException("ACCOUNT_ALREADY_LINKED");
            }

            customer.setProvider("GOOGLE");

            if (customer.getProviderId() == null) {
                customer.setProviderId(request.getProviderId());
            }
            if (customer.getAvatarUrl() == null) {
                customer.setAvatarUrl(request.getAvatarUrl());
            }
            if (customer.getFullName() == null) {
                customer.setFullName(request.getFullName());
            }

            if (!"ACTIVE".equals(customer.getStatus())) {
                throw new RuntimeException("ACCOUNT_DISABLED");
            }

            customerRepository.save(customer);
        }

        return CustomerAuthResponse.builder()
                .id(customer.getId())
                .email(customer.getEmail())
                .status(customer.getStatus())
                .build();
    }
}
