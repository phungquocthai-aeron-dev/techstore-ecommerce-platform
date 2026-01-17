package com.techstore.user.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAuthResponse {
    private Long id;
    private String email;
    private String password;
    private String status;
}
