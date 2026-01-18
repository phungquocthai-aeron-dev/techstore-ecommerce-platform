package com.techstore.user.service;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MailService {

    public void sendStaffAccount(String email, String password) {
        log.info("=== SEND MAIL DEMO ===");
        log.info("To: {}", email);
        log.info("Username: {}", email);
        log.info("Password: {}", password);
        log.info("=====================");
    }
}
