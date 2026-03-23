package com.techstore.identity.configuration;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

    @Bean(name = "userWebClient")
    WebClient userServiceClient() {
        HttpClient httpClient = HttpClient.create().responseTimeout(Duration.ofSeconds(5));

        return WebClient.builder()
                .baseUrl("http://localhost:8081/user")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
