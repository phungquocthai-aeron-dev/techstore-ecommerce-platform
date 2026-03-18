package com.techstore.user.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.techstore.event.dto.OtpSendEvent;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OtpEventProducer {

    KafkaTemplate<String, OtpSendEvent> kafkaTemplate;

    static final String TOPIC = "notification.otp.send";

    public void sendOtpEvent(String email, String userType) {
        OtpSendEvent event =
                OtpSendEvent.builder().email(email).userType(userType).build();
        kafkaTemplate.send(TOPIC, email, event);
    }
}
