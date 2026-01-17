package com.techstore.user.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffAuthResponse {
    private Long id;
    private String email;
    private String password;
    private String status;
    private String scope;
}
