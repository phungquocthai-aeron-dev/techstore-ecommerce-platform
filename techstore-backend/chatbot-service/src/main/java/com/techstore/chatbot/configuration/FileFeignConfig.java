package com.techstore.chatbot.configuration;

import java.time.Duration;

import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import feign.Request;
import feign.RequestInterceptor;
import feign.codec.Decoder;
import feign.codec.ErrorDecoder;
import feign.optionals.OptionalDecoder;

@Configuration
public class FileFeignConfig {

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
        return new FileFeignErrorDecoder();
    }

    @Bean
    Decoder feignDecoder() {

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(mapper);

        return new OptionalDecoder(
                new ResponseEntityDecoder(new SpringDecoder(() -> new HttpMessageConverters(converter))));
    }
}
