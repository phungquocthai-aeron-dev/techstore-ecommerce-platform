package com.techstore.identity.client.dto;

import lombok.*;

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
