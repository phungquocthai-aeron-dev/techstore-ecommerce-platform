package com.techstore.cart.presentation.mapper;

import com.techstore.cart.domain.model.Cart;
import com.techstore.cart.domain.model.CartItem;
import com.techstore.cart.presentation.dto.CartItemResponse;
import com.techstore.cart.presentation.dto.CartResponse;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * CartPresentationMapper - Converts domain models to HTTP response DTOs.
 * Lives in the presentation layer; domain has no knowledge of this mapper.
 */
@Component
public class CartPresentationMapper {

    public CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(this::toItemResponse)
                .toList();

        return CartResponse.builder()
                .cartId(cart.getId())
                .customerId(cart.getCustomerId())
                .totalPrice(cart.getTotalPrice())
                .items(items)
                .build();
    }

    public CartItemResponse toItemResponse(CartItem item) {
        return CartItemResponse.builder()
                .id(item.getId())
                .variantId(item.getVariantId())
                .quantity(item.getQuantity())
                .price(item.getPriceSnapshot())
                .subTotal(item.getSubTotal())
                .build();
    }
}
