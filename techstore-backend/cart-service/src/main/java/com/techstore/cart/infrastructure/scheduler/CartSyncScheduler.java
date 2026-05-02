package com.techstore.cart.infrastructure.scheduler;

import com.techstore.cart.domain.model.Cart;
import com.techstore.cart.infrastructure.persistence.MysqlCartRepository;
import com.techstore.cart.infrastructure.redis.RedisCartRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

/**
 * CartSyncScheduler - Runs every 5 minutes.
 *
 * Algorithm:
 *   1. Scan all dirty keys: cart:dirty:*
 *   2. For each dirty key, extract customerId
 *   3. Load cart from Redis
 *   4. Save to MySQL
 *   5. Clear dirty flag
 *
 * Only carts modified since last sync are processed — minimises MySQL writes.
 */
@Slf4j
@Component
public class CartSyncScheduler {

    private static final String DIRTY_KEY_PATTERN = "cart:dirty:*";

    private final RedisTemplate<String, Object> scanRedisTemplate;
    private final RedisCartRepository redisCartRepository;
    private final MysqlCartRepository mysqlCartRepository;

    public CartSyncScheduler(
            @Qualifier("scanRedisTemplate") RedisTemplate<String, Object> scanRedisTemplate,
            RedisCartRepository redisCartRepository,
            MysqlCartRepository mysqlCartRepository) {
        this.scanRedisTemplate     = scanRedisTemplate;
        this.redisCartRepository   = redisCartRepository;
        this.mysqlCartRepository   = mysqlCartRepository;
    }

    @Scheduled(fixedDelay = 5 * 60 * 1000, initialDelay = 60 * 1000)
    public void syncDirtyCartsToMysql() {
        log.info("[CartSync] Starting sync job...");
        int synced = 0;
        int skipped = 0;
        int failed  = 0;

        Set<String> dirtyKeys = scanRedisTemplate.keys(DIRTY_KEY_PATTERN);

        if (dirtyKeys == null || dirtyKeys.isEmpty()) {
            log.info("[CartSync] No dirty carts to sync.");
            return;
        }

        log.info("[CartSync] Found {} dirty cart(s) to sync.", dirtyKeys.size());

        for (String dirtyKey : dirtyKeys) {
            try {
                Long customerId = extractCustomerId(dirtyKey);
                if (customerId == null) {
                    skipped++;
                    continue;
                }

                Optional<Cart> cartOpt = redisCartRepository.findByCustomerId(customerId);

                if (cartOpt.isEmpty()) {
                    // Cart expired from Redis before sync (TTL elapsed) — just clear dirty flag
                    log.warn("[CartSync] Cart for customer {} no longer in Redis. Clearing dirty flag.", customerId);
                    redisCartRepository.clearDirty(customerId);
                    skipped++;
                    continue;
                }

                mysqlCartRepository.save(cartOpt.get());
                redisCartRepository.clearDirty(customerId);
                synced++;
                log.debug("[CartSync] Synced cart for customer {}.", customerId);

            } catch (Exception e) {
                log.error("[CartSync] Failed to sync dirty key '{}': {}", dirtyKey, e.getMessage(), e);
                failed++;
            }
        }

        log.info("[CartSync] Finished. Synced={}, Skipped={}, Failed={}", synced, skipped, failed);
    }

    private Long extractCustomerId(String dirtyKey) {
        // Key format: cart:dirty:{customerId}
        try {
            String[] parts = dirtyKey.split(":");
            return Long.parseLong(parts[parts.length - 1]);
        } catch (NumberFormatException e) {
            log.warn("[CartSync] Cannot parse customerId from key: {}", dirtyKey);
            return null;
        }
    }
}
