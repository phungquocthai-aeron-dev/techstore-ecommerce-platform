package com.techstore.cart.infrastructure.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * RedisConfig
 *  - cartRedisTemplate  : typed <String, RedisCart> — used by RedisCartRepository
 *  - scanRedisTemplate  : generic <String, Object>  — used by CartSyncScheduler for key scanning
 *
 * TTL is injected from cart.ttl (seconds), defaulting to 604800 (7 days).
 */
@Configuration
public class RedisConfig {

    @Value("${cart.ttl:604800}")
    private long cartTtlSeconds;

    @Bean
    @Primary
    public RedisTemplate<String, RedisCart> cartRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, RedisCart> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        ObjectMapper mapper = buildObjectMapper();
        Jackson2JsonRedisSerializer<RedisCart> serializer =
                new Jackson2JsonRedisSerializer<>(mapper, RedisCart.class);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }

    @Bean("scanRedisTemplate")
    public RedisTemplate<String, Object> scanRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    public long getCartTtlSeconds() {
        return cartTtlSeconds;
    }

    private ObjectMapper buildObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}