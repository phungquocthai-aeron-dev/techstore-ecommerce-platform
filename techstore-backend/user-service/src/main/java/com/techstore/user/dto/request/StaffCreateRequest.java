package com.techstore.user.dto.request;

import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StaffCreateRequest {
    private String email;
    private String password;
    private String fullName;
    private String phone;
    private Set<String> roles;
}
