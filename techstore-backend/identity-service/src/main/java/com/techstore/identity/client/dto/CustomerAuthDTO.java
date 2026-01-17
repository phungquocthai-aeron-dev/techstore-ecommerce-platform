package com.techstore.identity.client.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAuthDTO {
    private Long id;
    private String email;
    private String password;
    private String status;
}
