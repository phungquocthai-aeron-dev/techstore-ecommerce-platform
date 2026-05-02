package com.techstore.cart.infrastructure;

import com.techstore.cart.domain.model.Cart;
import com.techstore.cart.domain.repository.CartRepository;
import com.techstore.cart.infrastructure.persistence.MysqlCartRepository;
import com.techstore.cart.infrastructure.redis.RedisCartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * CartRepositoryAdapter - Implements domain CartRepository port.
 *
 * Read strategy:
 *   1. Try Redis first (hot data)
 *   2. On miss → load from MySQL, warm up Redis, return
 *
 * Write strategy:
 *   - Always write to Redis only
 *   - Mark dirty flag in Redis
 *   - MySQL sync happens via CronJob every 5 minutes
 *
 * Delete strategy:
 *   - Remove from both Redis and MySQL
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CartRepositoryAdapter implements CartRepository {

    private final RedisCartRepository redisRepo;
    private final MysqlCartRepository mysqlRepo;

    @Override
    public Optional<Cart> findByCustomerId(Long customerId) {
        // 1. Try Redis
        Optional<Cart> cached = redisRepo.findByCustomerId(customerId);
        if (cached.isPresent()) {
            return cached;
        }

        // 2. Cache miss → load from MySQL and warm up Redis
        Optional<Cart> fromDb = mysqlRepo.findByCustomerId(customerId);
        fromDb.ifPresent(cart -> {
            log.info("Cache miss for customer {}. Loading from MySQL and warming Redis.", customerId);
            redisRepo.warmUp(cart); // does NOT set dirty flag
        });

        return fromDb;
    }

    @Override
    public Cart save(Cart cart) {
        // Write-through to Redis only; MySQL sync deferred to cron
        return redisRepo.save(cart);
    }

    @Override
    public void deleteByCustomerId(Long customerId) {
        redisRepo.delete(customerId);
        mysqlRepo.deleteByCustomerId(customerId);
    }
}
