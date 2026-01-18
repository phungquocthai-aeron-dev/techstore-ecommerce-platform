package com.techstore.user.dto.response;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerResponse {
    private Long id;
    private String email;
    private String phone;
    private String fullName;
    private String avatarUrl;
    private String provider;
    private String status;
    private LocalDate dob;
}
