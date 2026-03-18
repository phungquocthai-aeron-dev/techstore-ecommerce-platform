package com.techstore.notification.dto.response;

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
