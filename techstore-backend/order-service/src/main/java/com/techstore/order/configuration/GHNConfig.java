package com.techstore.order.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "app.ghn")
@Getter
@Setter
public class GHNConfig {
    private String baseUrl;
    private String token;
}
