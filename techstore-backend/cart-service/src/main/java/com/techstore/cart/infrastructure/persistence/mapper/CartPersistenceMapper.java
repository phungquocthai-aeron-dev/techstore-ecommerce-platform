package com.techstore.cart.infrastructure.persistence.mapper;

import com.techstore.cart.domain.model.Cart;
import com.techstore.cart.domain.model.CartItem;
import com.techstore.cart.infrastructure.persistence.entity.CartDetailJpaEntity;
import com.techstore.cart.infrastructure.persistence.entity.CartJpaEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Persistence mapper - converts between JPA entities and domain models.
 * No MapStruct here to keep infra mapping explicit and decoupled.
 */
@Component
public class CartPersistenceMapper {

    public Cart toDomain(CartJpaEntity entity) {
        if (entity == null) return null;

        List<CartItem> items = entity.getDetails() == null ? new ArrayList<>() :
                entity.getDetails().stream().map(this::toDomainItem).toList();

        return Cart.builder()
                .id(entity.getId())
                .customerId(entity.getCustomerId())
                .items(new ArrayList<>(items))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public CartItem toDomainItem(CartDetailJpaEntity detail) {
        return CartItem.builder()
                .id(detail.getId())
                .variantId(detail.getVariantId())
                .quantity(detail.getQuantity())
                .priceSnapshot(detail.getPriceSnapshot())
                .addedAt(detail.getAddedAt())
                .build();
    }

    public CartDetailJpaEntity toJpaDetail(CartItem item, CartJpaEntity cartEntity) {
        CartDetailJpaEntity detail = new CartDetailJpaEntity();
        // Only set ID if it's a real persisted ID (positive), skip temporary negative IDs
        if (item.getId() != null && item.getId() > 0) {
            detail.setId(item.getId());
        }
        detail.setVariantId(item.getVariantId());
        detail.setQuantity(item.getQuantity());
        detail.setPriceSnapshot(item.getPriceSnapshot());
        detail.setCart(cartEntity);
        return detail;
    }

    public List<CartDetailJpaEntity> toJpaDetails(List<CartItem> items, CartJpaEntity cartEntity) {
        return items.stream().map(i -> toJpaDetail(i, cartEntity)).toList();
    }
}
