package com.techstore.user.dto.request;

import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StaffRoleUpdateRequest {
    private Set<String> roleNames;
}
