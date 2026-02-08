package com.techstore.user.dto.request;

import java.util.Set;

import com.techstore.user.validator.PhoneNumber;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StaffRequest {
    private Long id;
    private String fullName;
    private String email;

    @PhoneNumber(message = "INVALID_PHONE")
    private String phone;

    private String status;
    private Set<String> roleNames;
}
