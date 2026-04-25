package com.techstore.chatbot;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "app.gemini")
@Data
public class GeminiConfig {
    private List<ApiKey> apiKeys = new ArrayList<>();
    private String url;

    @Data
    public static class ApiKey {
        private String key;
    }
}
