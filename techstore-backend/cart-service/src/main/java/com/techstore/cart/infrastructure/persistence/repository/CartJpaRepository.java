package com.techstore.cart.infrastructure.persistence.repository;

import com.techstore.cart.infrastructure.persistence.entity.CartJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartJpaRepository extends JpaRepository<CartJpaEntity, Long> {
    Optional<CartJpaEntity> findByCustomerId(Long customerId);
}
