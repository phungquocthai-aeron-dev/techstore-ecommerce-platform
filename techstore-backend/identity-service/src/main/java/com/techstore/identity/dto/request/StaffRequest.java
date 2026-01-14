package com.techstore.identity.dto.request;

import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StaffRequest {
    private String fullName;
    private String email;
    private String phone;
    private String status;
    private Set<Long> roleIds;
}

