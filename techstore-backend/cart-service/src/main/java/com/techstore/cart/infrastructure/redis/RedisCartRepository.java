package com.techstore.cart.infrastructure.redis;

import com.techstore.cart.domain.model.Cart;
import com.techstore.cart.domain.model.CartItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * RedisCartRepository
 *
 * Key scheme:
 *   cart:user:{customerId}   → stores the full RedisCart JSON
 *   cart:dirty:{customerId}  → marker that signals MySQL sync is needed
 *
 * TTL is read from cart.ttl (seconds) — default 604800 (7 days).
 * Both keys share the same TTL and are reset on every write.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisCartRepository {

    public static final String CART_KEY_PREFIX  = "cart:user:";
    public static final String DIRTY_KEY_PREFIX = "cart:dirty:";

    private final RedisTemplate<String, RedisCart> redisTemplate;

    @Value("${cart.ttl:604800}")
    private long cartTtlSeconds;

    // ────────────────────────────────────────────── read

    public Optional<Cart> findByCustomerId(Long customerId) {
        RedisCart cached = redisTemplate.opsForValue().get(cartKey(customerId));
        if (cached == null) {
            log.debug("[Redis] Cache miss — customer={}", customerId);
            return Optional.empty();
        }
        log.debug("[Redis] Cache hit — customer={}", customerId);
        return Optional.of(toDomain(cached));
    }

    // ────────────────────────────────────────────── write

    /**
     * Persists cart to Redis and marks it dirty for MySQL sync.
     */
    public Cart save(Cart cart) {
        Duration ttl = Duration.ofSeconds(cartTtlSeconds);
        RedisCart payload = toRedis(cart);

        redisTemplate.opsForValue().set(cartKey(cart.getCustomerId()), payload, ttl);
        redisTemplate.opsForValue().set(dirtyKey(cart.getCustomerId()), payload, ttl);

        log.debug("[Redis] Saved & marked dirty — customer={}", cart.getCustomerId());
        return cart;
    }

    /**
     * Loads cart into Redis after a MySQL warm-up.
     * Does NOT set the dirty flag — data is already in sync with MySQL.
     */
    public void warmUp(Cart cart) {
        redisTemplate.opsForValue().set(
                cartKey(cart.getCustomerId()),
                toRedis(cart),
                Duration.ofSeconds(cartTtlSeconds)
        );
        log.debug("[Redis] Warmed up cache — customer={}", cart.getCustomerId());
    }

    // ────────────────────────────────────────────── delete

    public void delete(Long customerId) {
        redisTemplate.delete(cartKey(customerId));
        redisTemplate.delete(dirtyKey(customerId));
        log.debug("[Redis] Deleted cart & dirty flag — customer={}", customerId);
    }

    // ────────────────────────────────────────────── dirty-flag helpers

    public boolean isDirty(Long customerId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(dirtyKey(customerId)));
    }

    public void clearDirty(Long customerId) {
        redisTemplate.delete(dirtyKey(customerId));
    }

    // ────────────────────────────────────────────── key helpers

    public static String cartKey(Long customerId) {
        return CART_KEY_PREFIX + customerId;
    }

    public static String dirtyKey(Long customerId) {
        return DIRTY_KEY_PREFIX + customerId;
    }

    // ────────────────────────────────────────────── mapping

    public Cart toDomain(RedisCart r) {
        List<CartItem> items = r.getItems() == null ? new ArrayList<>() :
                r.getItems().stream().map(i -> CartItem.builder()
                        .id(i.getId())
                        .variantId(i.getVariantId())
                        .quantity(i.getQuantity())
                        .priceSnapshot(i.getPriceSnapshot())
                        .addedAt(i.getAddedAt())
                        .build()).toList();

        return Cart.builder()
                .customerId(r.getCustomerId())
                .items(new ArrayList<>(items))
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }

    private RedisCart toRedis(Cart cart) {
        List<RedisCart.RedisCartItem> items = cart.getItems().stream()
                .map(i -> RedisCart.RedisCartItem.builder()
                        .id(i.getId())
                        .variantId(i.getVariantId())
                        .quantity(i.getQuantity())
                        .priceSnapshot(i.getPriceSnapshot())
                        .addedAt(i.getAddedAt())
                        .build()).toList();

        return RedisCart.builder()
                .customerId(cart.getCustomerId())
                .items(new ArrayList<>(items))
                .createdAt(cart.getCreatedAt() != null ? cart.getCreatedAt() : LocalDateTime.now())
                .updatedAt(cart.getUpdatedAt() != null ? cart.getUpdatedAt() : LocalDateTime.now())
                .build();
    }
}