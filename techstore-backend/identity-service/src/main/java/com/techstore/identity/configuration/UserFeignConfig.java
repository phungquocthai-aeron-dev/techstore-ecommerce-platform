package com.techstore.identity.configuration;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.Request;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;

@Configuration
public class UserFeignConfig {

    @Bean
    Request.Options feignRequestOptions() {
        return new Request.Options(Duration.ofSeconds(3), Duration.ofSeconds(3), true);
    }

    @Bean
    RequestInterceptor authenticationRequestInterceptor() {
        return new AuthenticationRequestInterceptor();
    }

    @Bean
    ErrorDecoder errorDecoder() {
        return new UserFeignErrorDecoder();
    }
}
