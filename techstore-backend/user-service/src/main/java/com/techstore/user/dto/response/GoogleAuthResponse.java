package com.techstore.user.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GoogleAuthResponse {
    private Long customerId;
    private String status;
}
