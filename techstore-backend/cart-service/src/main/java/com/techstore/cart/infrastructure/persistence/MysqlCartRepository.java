package com.techstore.cart.infrastructure.persistence;

import com.techstore.cart.domain.model.Cart;
import com.techstore.cart.infrastructure.persistence.entity.CartDetailJpaEntity;
import com.techstore.cart.infrastructure.persistence.entity.CartJpaEntity;
import com.techstore.cart.infrastructure.persistence.mapper.CartPersistenceMapper;
import com.techstore.cart.infrastructure.persistence.repository.CartDetailJpaRepository;
import com.techstore.cart.infrastructure.persistence.repository.CartJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * MysqlCartRepository - Handles durable storage of cart data to MySQL.
 * Only called by:
 *   1. CronJob (sync dirty carts from Redis)
 *   2. CartRepository adapter (fallback on Redis cache miss)
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class MysqlCartRepository {

    private final CartJpaRepository cartJpaRepository;
    private final CartDetailJpaRepository cartDetailJpaRepository;
    private final CartPersistenceMapper mapper;

    @Transactional(readOnly = true)
    public Optional<Cart> findByCustomerId(Long customerId) {
        return cartJpaRepository.findByCustomerId(customerId).map(entity -> {
            // Force load lazy details
            entity.getDetails().size();
            return mapper.toDomain(entity);
        });
    }

    /**
     * Full replace - deletes existing details and re-inserts.
     * Safe because this runs on 5-min cron, not hot path.
     */
    @Transactional
    public Cart save(Cart cart) {
        CartJpaEntity cartEntity = cartJpaRepository.findByCustomerId(cart.getCustomerId())
                .orElseGet(() -> {
                    CartJpaEntity e = new CartJpaEntity();
                    e.setCustomerId(cart.getCustomerId());
                    return e;
                });

        cartJpaRepository.save(cartEntity);

        // Replace all details
        cartDetailJpaRepository.deleteByCartId(cartEntity.getId());

        List<CartDetailJpaEntity> details = mapper.toJpaDetails(cart.getItems(), cartEntity);
        cartDetailJpaRepository.saveAll(details);

        cartEntity.setDetails(details);
        log.debug("Synced cart to MySQL for customer {}", cart.getCustomerId());
        return mapper.toDomain(cartEntity);
    }

    @Transactional
    public void deleteByCustomerId(Long customerId) {
        cartJpaRepository.findByCustomerId(customerId).ifPresent(entity -> {
            cartDetailJpaRepository.deleteByCartId(entity.getId());
            cartJpaRepository.delete(entity);
        });
    }
}
