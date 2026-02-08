package com.techstore.cart.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techstore.cart.entity.CartDetail;

public interface CartDetailRepository extends JpaRepository<CartDetail, Long> {
    Optional<CartDetail> findByCartIdAndVariantId(Long cartId, Long variantId);

    List<CartDetail> findByIdIn(List<Long> ids);

    List<CartDetail> findByIdInAndCartId(List<Long> ids, Long cartId);
}
