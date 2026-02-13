package com.techstore.cart.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.techstore.cart.dto.request.AddItemRequest;
import com.techstore.cart.dto.request.CheckoutRequest;
import com.techstore.cart.dto.request.UpdateQuantityRequest;
import com.techstore.cart.response.ApiResponse;
import com.techstore.cart.response.CartResponse;
import com.techstore.cart.service.CartService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ApiResponse<CartResponse> getCart() {
        return ApiResponse.<CartResponse>builder().result(cartService.getCart()).build();
    }

    @PostMapping("/items")
    public ApiResponse<Void> addItem(@RequestBody AddItemRequest requestDTO) {
        cartService.addItem(requestDTO);
        return ApiResponse.<Void>builder().build();
    }

    @PutMapping("/items/{variantId}")
    public ApiResponse<Void> updateQuantity(
            @PathVariable Long variantId, @RequestBody UpdateQuantityRequest requestDTO) {

        cartService.updateQuantity(variantId, requestDTO);
        return ApiResponse.<Void>builder().build();
    }

    @DeleteMapping("/items/{variantId}")
    public ApiResponse<Void> removeItem(@PathVariable Long variantId) {

        cartService.removeItem(variantId);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/checkout")
    public ApiResponse<Void> checkout(@RequestBody CheckoutRequest requestDTO) {

        cartService.checkout(requestDTO);
        return ApiResponse.<Void>builder().build();
    }
}
