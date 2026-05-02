package com.techstore.cart.presentation.controller;

import com.techstore.cart.application.dto.request.AddItemCommand;
import com.techstore.cart.application.dto.request.CheckoutCommand;
import com.techstore.cart.application.dto.request.UpdateQuantityCommand;
import com.techstore.cart.application.usecase.AddItemUseCase;
import com.techstore.cart.application.usecase.CheckoutUseCase;
import com.techstore.cart.application.usecase.GetCartUseCase;
import com.techstore.cart.application.usecase.RemoveItemUseCase;
import com.techstore.cart.application.usecase.UpdateQuantityUseCase;
import com.techstore.cart.domain.model.Cart;
import com.techstore.cart.presentation.SecurityContextHelper;
import com.techstore.cart.presentation.dto.AddItemRequest;
import com.techstore.cart.presentation.dto.ApiResponse;
import com.techstore.cart.presentation.dto.CartResponse;
import com.techstore.cart.presentation.dto.CheckoutRequest;
import com.techstore.cart.presentation.dto.UpdateQuantityRequest;
import com.techstore.cart.presentation.mapper.CartPresentationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * CartController - HTTP entry point.
 * Responsibilities: parse request, resolve customer ID from JWT, delegate to use case, map response.
 * NO business logic here.
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
public class CartController {

    private final SecurityContextHelper securityContext;
    private final CartPresentationMapper mapper;

    private final GetCartUseCase getCartUseCase;
    private final AddItemUseCase addItemUseCase;
    private final UpdateQuantityUseCase updateQuantityUseCase;
    private final RemoveItemUseCase removeItemUseCase;
    private final CheckoutUseCase checkoutUseCase;

    @GetMapping("/mycart")
    public ApiResponse<CartResponse> getCart() {
        Long customerId = securityContext.getAuthenticatedCustomerId();
        Cart cart = getCartUseCase.execute(customerId);
        return ApiResponse.ok(mapper.toResponse(cart));
    }

    @PostMapping("/items")
    public ApiResponse<Void> addItem(@RequestBody AddItemRequest request) {
        Long customerId = securityContext.getAuthenticatedCustomerId();
        addItemUseCase.execute(customerId, new AddItemCommand(
                request.getVariantId(),
                request.getQuantity()
        ));
        return ApiResponse.ok();
    }

    @PutMapping("/items/{variantId}")
    public ApiResponse<Void> updateQuantity(
            @PathVariable Long variantId,
            @RequestBody UpdateQuantityRequest request) {
        Long customerId = securityContext.getAuthenticatedCustomerId();
        updateQuantityUseCase.execute(customerId, new UpdateQuantityCommand(
                variantId,
                request.getQuantity()
        ));
        return ApiResponse.ok();
    }

    @DeleteMapping("/items/{variantId}")
    public ApiResponse<Void> removeItem(@PathVariable Long variantId) {
        Long customerId = securityContext.getAuthenticatedCustomerId();
        removeItemUseCase.execute(customerId, variantId);
        return ApiResponse.ok();
    }

    @PostMapping("/checkout")
    public ApiResponse<Void> checkout(@RequestBody CheckoutRequest request) {
        Long customerId = securityContext.getAuthenticatedCustomerId();
        checkoutUseCase.execute(new CheckoutCommand(
                customerId,
                request.getCartDetailIds()
        ));
        return ApiResponse.ok();
    }
}
