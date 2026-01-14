package com.techstore.identity.dto.request;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerRegisterRequest {
    private String email;
    private String password;
    private String fullName;
    private String phone;
    private LocalDate dob;
}

