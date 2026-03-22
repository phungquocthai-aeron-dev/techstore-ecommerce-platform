package com.techstore.chatbot.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techstore.chatbot.exception.AppException;
import com.techstore.chatbot.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

    @Value("${app.gemini.api-key}")
    private String apiKey;

    @Value("${app.gemini.url}")
    private String geminiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public String generateContent(String prompt) {
        try {
            String url = geminiUrl + "?key=" + apiKey;

            // Log prompt (cắt ngắn nếu quá dài)
            String promptPreview = prompt.length() > 200 ? prompt.substring(0, 200) + "... [truncated]" : prompt;
            log.info("  [Gemini] ► Sending request");
            log.info("  [Gemini]   url    : {}", geminiUrl);
            log.info("  [Gemini]   prompt : \"{}\"", promptPreview.replace("\n", "\\n"));

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                    "generationConfig", Map.of("temperature", 0.7, "maxOutputTokens", 1024));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            long t0 = System.currentTimeMillis();
            String responseJson = restTemplate.postForObject(url, request, String.class);
            long elapsed = System.currentTimeMillis() - t0;

            log.info("  [Gemini] ◄ Response received in {} ms", elapsed);

            String text = extractTextFromResponse(responseJson);

            // Log usage metadata nếu có
            logUsageMetadata(responseJson);

            String textPreview = text.length() > 150 ? text.substring(0, 150) + "... [truncated]" : text;
            log.info("  [Gemini]   result : \"{}\"", textPreview.replace("\n", "\\n"));

            return text;

        } catch (AppException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("  [Gemini] ✗ Call failed: {}", ex.getMessage(), ex);
            throw new AppException(ErrorCode.GEMINI_API_ERROR);
        }
    }

    private String extractTextFromResponse(String responseJson) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);
            return root.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
        } catch (Exception ex) {
            log.error("  [Gemini] ✗ Failed to parse response: {}", responseJson);
            throw new AppException(ErrorCode.GEMINI_API_ERROR);
        }
    }

    private void logUsageMetadata(String responseJson) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode usage = root.path("usageMetadata");
            if (!usage.isMissingNode()) {
                log.info(
                        "  [Gemini]   tokens — prompt: {}, output: {}, total: {}",
                        usage.path("promptTokenCount").asInt(),
                        usage.path("candidatesTokenCount").asInt(),
                        usage.path("totalTokenCount").asInt());
            }
            JsonNode modelVersion = root.path("modelVersion");
            if (!modelVersion.isMissingNode()) {
                log.info("  [Gemini]   model  : {}", modelVersion.asText());
            }
        } catch (Exception ignored) {
            // metadata logging không quan trọng, bỏ qua nếu lỗi
        }
    }
}
