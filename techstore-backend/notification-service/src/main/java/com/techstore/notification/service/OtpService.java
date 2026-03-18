package com.techstore.notification.service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OtpService {

    RedisTemplate<String, String> redisTemplate;

    @Value("${notification.otp.expiration:300}")
    @NonFinal
    long otpExpiration;

    static final String OTP_PREFIX = "OTP:";

    public String generateAndSaveOtp(String email) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        redisTemplate.opsForValue().set(OTP_PREFIX + email, otp, otpExpiration, TimeUnit.SECONDS);
        return otp;
    }

    public boolean verifyOtp(String email, String otp) {
        String saved = redisTemplate.opsForValue().get(OTP_PREFIX + email);
        if (saved != null && saved.equals(otp)) {
            redisTemplate.delete(OTP_PREFIX + email); // Dùng 1 lần rồi xóa
            return true;
        }
        return false;
    }
}
