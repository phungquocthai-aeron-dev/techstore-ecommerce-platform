package com.techstore.chatbot.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techstore.chatbot.exception.AppException;
import com.techstore.chatbot.exception.ErrorCode;
import com.techstore.chatbot.exception.QuotaExceededException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

    @Value("${app.gemini.url}")
    private String geminiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final GeminiKeyManager keyManager;

    private static final int MAX_KEY_RETRIES = 4; // tối đa số key thử

    private static final int MAX_RETRY_503 = 3;
    private static final long RETRY_DELAY_MS = 1000; // 1 giây giữa các lần retry

    public String generateContent(String prompt) {
        log.info("[Gemini] >>> generateContent() CALLED");
        String currentKey = keyManager.currentKey();

        if (currentKey == null || currentKey.isBlank()) {
            log.error("[Gemini] API key NULL hoặc rỗng!");
            throw new RuntimeException("API key is NULL");
        }

        int keyAttempts = 0;
        while (keyAttempts < MAX_KEY_RETRIES) {
            try {
                return doRequestWithRetry(prompt, currentKey); // <-- wrap retry 503 ở đây

            } catch (QuotaExceededException ex) {
                String nextKey = keyManager.rotateKey(currentKey);
                if (nextKey == null) throw new AppException(ErrorCode.GEMINI_QUOTA_EXCEEDED);
                currentKey = nextKey;
                keyAttempts++;

            } catch (AppException ex) {
                throw ex;
            } catch (Exception ex) {
                log.error("[Gemini] Unexpected error: {}", ex.getMessage(), ex);
                throw new AppException(ErrorCode.GEMINI_API_ERROR);
            }
        }

        throw new AppException(ErrorCode.GEMINI_QUOTA_EXCEEDED);
    }

    //    public String generateContent(String prompt) {
    //        log.info("[Gemini] >>> generateContent() CALLED");
    //        int attempts = 0;
    //        String currentKey = keyManager.currentKey();
    //
    //        if (currentKey == null || currentKey.isBlank()) {
    //            log.error("[Gemini] API key NULL hoặc rỗng!");
    //            throw new RuntimeException("API key is NULL");
    //        }
    //
    //        log.info("[Gemini] Using key (first): {}****", currentKey.substring(0, 6));
    //
    //        while (attempts < MAX_KEY_RETRIES) {
    //
    //            try {
    //                return doRequest(prompt, currentKey);
    //
    //            } catch (QuotaExceededException ex) {
    //                // Hết quota → rotate key và thử lại
    //                String nextKey = keyManager.rotateKey(currentKey);
    //                if (nextKey == null) {
    //                    throw new AppException(ErrorCode.GEMINI_QUOTA_EXCEEDED);
    //                }
    //                attempts++;
    //
    //            } catch (AppException ex) {
    //                throw ex;
    //            } catch (Exception ex) {
    //                log.error("[Gemini] Unexpected error: {}", ex.getMessage(), ex);
    //                throw new AppException(ErrorCode.GEMINI_API_ERROR);
    //            }
    //        }
    //
    //        throw new AppException(ErrorCode.GEMINI_QUOTA_EXCEEDED);
    //    }

    private String doRequestWithRetry(String prompt, String apiKey) {
        int attempt = 0;
        while (attempt < MAX_RETRY_503) {
            try {
                return doRequest(prompt, apiKey);

            } catch (HttpServerErrorException ex) {
                if (ex.getStatusCode().value() == 503) {
                    attempt++;
                    log.warn(
                            "[Gemini] 503 UNAVAILABLE — retry {}/{} sau {}ms",
                            attempt,
                            MAX_RETRY_503,
                            RETRY_DELAY_MS * attempt);
                    if (attempt >= MAX_RETRY_503) {
                        log.error("[Gemini] Hết retry 503, bỏ cuộc");
                        throw new AppException(ErrorCode.GEMINI_API_ERROR);
                    }
                    try {
                        Thread.sleep(RETRY_DELAY_MS * attempt); // backoff tuyến tính: 1s, 2s, 3s
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new AppException(ErrorCode.GEMINI_API_ERROR);
                    }
                } else {
                    throw ex; // 4xx hoặc 5xx khác → không retry
                }
            }
        }
        throw new AppException(ErrorCode.GEMINI_API_ERROR);
    }

    private String doRequest(String prompt, String apiKey) {
        String url = geminiUrl + "?key=" + apiKey;

        log.info("🌍 [Gemini] CALLING API with key: ****{}", apiKey.substring(apiKey.length() - 6));

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig",
                        Map.of("response_mime_type", "application/json", "temperature", 0.1, "maxOutputTokens", 1024));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        long t0 = System.currentTimeMillis();

        try {
            String responseJson = restTemplate.postForObject(url, new HttpEntity<>(requestBody, headers), String.class);

            long elapsed = System.currentTimeMillis() - t0;

            log.info("╔════════════════ GEMINI RAW RESPONSE ════════════════╗");
            log.info("║ time: {} ms", elapsed);
            log.info("║ body: {}", responseJson);
            log.info("╚══════════════════════════════════════════════════════╝");

            checkForQuotaError(responseJson, apiKey);

            String text = extractTextFromResponse(responseJson);

            if (text == null || text.isBlank()) {
                throw new AppException(ErrorCode.GEMINI_API_ERROR);
            }

            return text;

        } catch (HttpClientErrorException.TooManyRequests ex) {
            String body = ex.getResponseBodyAsString();

            log.warn("[Gemini] 429 TOO MANY REQUESTS: {}", body);

            // 👉 PHÂN BIỆT quota vs rate limit
            if (body != null && body.contains("quota")) {
                log.warn("[Gemini] → QUOTA EXCEEDED → rotate key");
                throw new QuotaExceededException(apiKey);
            }

            // 👉 RATE LIMIT → sleep rồi retry lại cùng key
            long retryMs = extractRetryDelay(body);
            log.warn("[Gemini] → RATE LIMIT → sleep {} ms rồi retry", retryMs);

            sleep(retryMs);

            // retry lại 1 lần
            return doRequest(prompt, apiKey);

        } catch (HttpServerErrorException ex) {
            throw ex; // để doRequestWithRetry xử lý 503

        } catch (Exception ex) {
            log.error("[Gemini] Unexpected error: {}", ex.getMessage(), ex);
            throw new AppException(ErrorCode.GEMINI_API_ERROR);
        }
    }

    //    private String doRequest(String prompt, String apiKey) {
    //        String url = geminiUrl + "?key=" + apiKey;
    //        log.info("🌍 [Gemini] CALLING API with key: ****{}", apiKey.substring(apiKey.length() - 6));
    //
    //        Map<String, Object> requestBody = Map.of(
    //                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
    //                "generationConfig",
    //                        Map.of("response_mime_type", "application/json", "temperature", 0.1, "maxOutputTokens",
    // 1024));
    //
    //        HttpHeaders headers = new HttpHeaders();
    //        headers.setContentType(MediaType.APPLICATION_JSON);
    //
    //        long t0 = System.currentTimeMillis();
    //
    //        String responseJson = restTemplate.postForObject(url, new HttpEntity<>(requestBody, headers),
    // String.class);
    //
    //        long elapsed = System.currentTimeMillis() - t0;
    //
    //        log.info("╔════════════════ GEMINI RAW RESPONSE ════════════════╗");
    //        log.info("║ time: {} ms", elapsed);
    //        log.info("║ body: {}", responseJson);
    //        log.info("╚══════════════════════════════════════════════════════╝");
    //
    //        // Kiểm tra quota
    //        checkForQuotaError(responseJson, apiKey);
    //
    //        String text = extractTextFromResponse(responseJson);
    //
    //        log.info("╔════════════════ GEMINI TEXT ════════════════════════╗");
    //        log.info("║ text: {}", text);
    //        log.info("╚══════════════════════════════════════════════════════╝");
    //
    //        if (text == null || text.isBlank()) {
    //            throw new AppException(ErrorCode.GEMINI_API_ERROR);
    //        }
    //
    //        return text;
    //    }

    //    /** Gemini trả 200 nhưng body chứa lỗi RESOURCE_EXHAUSTED */
    //    private void checkForQuotaError(String responseJson, String usedKey) {
    //        try {
    //            JsonNode root = objectMapper.readTree(responseJson);
    //            JsonNode error = root.path("error");
    //            if (!error.isMissingNode()) {
    //                String status = error.path("status").asText("");
    //                String message = error.path("message").asText("");
    //                log.warn("[Gemini] API error — status: {}, message: {}", status, message);
    //
    //                if ("RESOURCE_EXHAUSTED".equals(status) || message.toLowerCase().contains("quota")) {
    //                    throw new QuotaExceededException("Quota exceeded for key: " + usedKey);
    //                }
    //                throw new AppException(ErrorCode.GEMINI_API_ERROR);
    //            }
    //        } catch (QuotaExceededException | AppException ex) {
    //            throw ex;
    //        } catch (Exception ignored) {
    //        }
    //    }

    private void checkForQuotaError(String responseJson, String apiKey) {

        if (responseJson.contains("QUOTA_EXCEEDED")) {
            log.warn("[Gemini] QUOTA exceeded for key: {}", apiKey.substring(apiKey.length() - 6));
            throw new QuotaExceededException(apiKey);
        }

        if (responseJson.contains("API_KEY_INVALID")) {
            log.error("[Gemini] INVALID key: {}", apiKey.substring(apiKey.length() - 6));
            throw new AppException(ErrorCode.GEMINI_API_ERROR);
        }
    }

    private String extractTextFromResponse(String responseJson) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);

            JsonNode textNode = root.path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text");

            if (textNode.isMissingNode()) {
                log.error("[Gemini] Missing text field: {}", responseJson);
                return null;
            }

            return textNode.asText();

        } catch (Exception ex) {
            log.error("  [Gemini] ✗ Failed to parse response: {}", responseJson);
            return null;
        }
    }

    //    private void logUsageMetadata(String responseJson) {
    //        try {
    //            JsonNode root = objectMapper.readTree(responseJson);
    //            JsonNode usage = root.path("usageMetadata");
    //            if (!usage.isMissingNode()) {
    //                log.info(
    //                        "  [Gemini]   tokens — prompt: {}, output: {}, total: {}",
    //                        usage.path("promptTokenCount").asInt(),
    //                        usage.path("candidatesTokenCount").asInt(),
    //                        usage.path("totalTokenCount").asInt());
    //            }
    //            JsonNode modelVersion = root.path("modelVersion");
    //            if (!modelVersion.isMissingNode()) {
    //                log.info("  [Gemini]   model  : {}", modelVersion.asText());
    //            }
    //        } catch (Exception ignored) {
    //            // metadata logging không quan trọng, bỏ qua nếu lỗi
    //        }
    //    }

    private long extractRetryDelay(String body) {
        try {
            if (body != null && body.contains("retryDelay")) {
                // ví dụ: "retryDelay": "11s"
                int start = body.indexOf("retryDelay");
                int quote1 = body.indexOf("\"", start + 12);
                int quote2 = body.indexOf("\"", quote1 + 1);

                String value = body.substring(quote1 + 1, quote2);
                long seconds = Long.parseLong(value.replace("s", ""));

                return seconds * 1000;
            }
        } catch (Exception ignored) {
        }

        return 3000; // fallback 3s
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
