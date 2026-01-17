package com.techstore.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffAuthDTO {
    private Long id;
    private String email;
    private String password;
    private String status;
    private String scope;
}
