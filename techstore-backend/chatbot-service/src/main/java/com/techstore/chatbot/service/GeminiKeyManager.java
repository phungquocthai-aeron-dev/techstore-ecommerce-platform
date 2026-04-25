package com.techstore.chatbot.service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import com.techstore.chatbot.GeminiConfig;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GeminiKeyManager {

    private final List<String> keys;
    private final AtomicInteger index = new AtomicInteger(0);
    private final Set<String> exhaustedKeys = ConcurrentHashMap.newKeySet();

    public GeminiKeyManager(GeminiConfig config) {
        this.keys = config.getApiKeys().stream()
                .map(GeminiConfig.ApiKey::getKey)
                .filter(k -> k != null && !k.isBlank())
                .toList();

        if (keys.isEmpty()) throw new IllegalStateException("No Gemini API keys configured");
        log.info("[GeminiKeyManager] Loaded {} API key(s)", keys.size());
    }

    /** Lấy key hiện tại (Round-Robin) */
    public String currentKey() {
        int idx = index.get() % keys.size();
        return keys.get(idx);
    }

    /**
     * Đánh dấu key hiện tại bị exhausted → rotate sang key tiếp theo.
     * @return key mới, hoặc null nếu tất cả đã hết quota
     */
    public String rotateKey(String failedKey) {
        exhaustedKeys.add(failedKey);
        log.warn(
                "[GeminiKeyManager] Key exhausted: ...{} | exhausted={}/{}",
                failedKey.substring(Math.max(0, failedKey.length() - 6)),
                exhaustedKeys.size(),
                keys.size());

        if (exhaustedKeys.size() >= keys.size()) {
            log.error("[GeminiKeyManager] All API keys exhausted!");
            return null;
        }

        // Tìm key tiếp theo chưa bị exhausted
        for (int i = 1; i <= keys.size(); i++) {
            int nextIdx = (index.get() + i) % keys.size();
            String candidate = keys.get(nextIdx);
            if (!exhaustedKeys.contains(candidate)) {
                index.set(nextIdx);
                log.info("[GeminiKeyManager] Rotated to key index {}", nextIdx);
                return candidate;
            }
        }
        return null;
    }

    /** Reset exhausted keys (ví dụ: schedule reset mỗi ngày) */
    public void resetExhausted() {
        exhaustedKeys.clear();
        log.info("[GeminiKeyManager] Exhausted keys reset");
    }
}
