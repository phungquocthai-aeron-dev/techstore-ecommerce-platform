package com.techstore.chatbot.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeminiKeyResetScheduler {

    private final GeminiKeyManager keyManager;

    // Reset lúc 00:05 mỗi ngày (quota Gemini free reset theo ngày)
    @Scheduled(cron = "0 5 0 * * *")
    public void resetDailyQuota() {
        keyManager.resetExhausted();
        log.info("[Scheduler] Gemini API keys quota reset");
    }
}
