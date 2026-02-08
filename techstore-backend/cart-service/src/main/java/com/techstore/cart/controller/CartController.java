package com.techstore.cart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.techstore.cart.dto.request.AddItemRequest;
import com.techstore.cart.dto.request.CheckoutRequest;
import com.techstore.cart.dto.request.UpdateQuantityRequest;
import com.techstore.cart.response.CartResponse;
import com.techstore.cart.service.CartService;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * Lấy giỏ hàng của customer
     */
    @GetMapping
    public CartResponse getCart(@RequestHeader("X-Customer-Id") Long customerId) {

        return cartService.getCart(customerId);
    }

    /**
     * Thêm item vào giỏ hàng
     */
    @PostMapping("/items")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addItem(@RequestHeader("X-Customer-Id") Long customerId, @RequestBody AddItemRequest requestDTO) {

        cartService.addItem(customerId, requestDTO);
    }

    /**
     * Cập nhật số lượng item
     */
    @PutMapping("/items/{variantId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateQuantity(
            @RequestHeader("X-Customer-Id") Long customerId,
            @PathVariable Long variantId,
            @RequestBody UpdateQuantityRequest requestDTO) {

        cartService.updateQuantity(customerId, variantId, requestDTO);
    }

    /**
     * Xóa item khỏi giỏ hàng
     */
    @DeleteMapping("/items/{variantId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeItem(@RequestHeader("X-Customer-Id") Long customerId, @PathVariable Long variantId) {

        cartService.removeItem(customerId, variantId);
    }

    /**
     * Checkout một phần giỏ hàng
     */
    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void checkout(@RequestHeader("X-Customer-Id") Long customerId, @RequestBody CheckoutRequest requestDTO) {

        cartService.checkout(customerId, requestDTO);
    }
}
