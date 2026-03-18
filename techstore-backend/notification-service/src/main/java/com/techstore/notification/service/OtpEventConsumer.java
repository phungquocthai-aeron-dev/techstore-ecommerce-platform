package com.techstore.notification.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.techstore.event.dto.OtpSendEvent;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OtpEventConsumer {

    OtpService otpService;
    EmailService emailService;

    @KafkaListener(topics = "notification.otp.send", groupId = "notification-group")
    public void handleOtpSendEvent(OtpSendEvent event) {
        // Sinh OTP → lưu Redis
        String otp = otpService.generateAndSaveOtp(event.getEmail());

        // Gửi email
        emailService.sendOtpEmail(event.getEmail(), otp);
    }
}
