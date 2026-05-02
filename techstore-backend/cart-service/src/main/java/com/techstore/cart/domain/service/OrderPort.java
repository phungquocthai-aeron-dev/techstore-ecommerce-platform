package com.techstore.cart.domain.service;

import com.techstore.cart.domain.model.Cart;
import com.techstore.cart.domain.model.CartItem;

import java.util.List;

/**
 * OrderPort - Domain port for placing orders.
 * Implemented in infrastructure layer via Feign client.
 */
public interface OrderPort {
    void createOrder(Long customerId, List<CartItem> items);
}
