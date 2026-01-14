package com.techstore.identity.dto.response;

import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StaffResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String status;
    private Set<RoleResponse> roles;
}

