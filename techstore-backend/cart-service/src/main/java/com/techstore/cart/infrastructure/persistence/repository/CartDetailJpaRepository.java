package com.techstore.cart.infrastructure.persistence.repository;

import com.techstore.cart.infrastructure.persistence.entity.CartDetailJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartDetailJpaRepository extends JpaRepository<CartDetailJpaEntity, Long> {
    void deleteByCartId(Long cartId);
}
