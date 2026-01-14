package com.techstore.identity.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.admin")
public class AdminAccountProperties {
    private String email;
    private String password;
}
