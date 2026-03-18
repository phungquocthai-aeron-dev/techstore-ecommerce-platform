package com.techstore.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpSendEvent {
    String email;
    String userType; // "CUSTOMER" hoặc "STAFF"
}
