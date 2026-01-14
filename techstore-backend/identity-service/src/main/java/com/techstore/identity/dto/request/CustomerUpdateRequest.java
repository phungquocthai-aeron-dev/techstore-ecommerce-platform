package com.techstore.identity.dto.request;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerUpdateRequest {
    private String fullName;
    private String phone;
    private String avatarUrl;
    private LocalDate dob;
    private String status;
}
