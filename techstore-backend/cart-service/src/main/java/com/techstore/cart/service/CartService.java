package com.techstore.cart.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techstore.cart.client.OrderClient;
import com.techstore.cart.client.ProductClient;
import com.techstore.cart.dto.request.AddItemRequest;
import com.techstore.cart.dto.request.CheckoutRequest;
import com.techstore.cart.dto.request.UpdateQuantityRequest;
import com.techstore.cart.entity.Cart;
import com.techstore.cart.entity.CartDetail;
import com.techstore.cart.exception.AppException;
import com.techstore.cart.exception.ErrorCode;
import com.techstore.cart.mapper.CartMapper;
import com.techstore.cart.repository.CartDetailRepository;
import com.techstore.cart.repository.CartRepository;
import com.techstore.cart.response.ApiResponse;
import com.techstore.cart.response.CartResponse;
import com.techstore.cart.response.VariantInfoResponse;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@Transactional
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartDetailRepository cartDetailRepository;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductClient productClient;

    @Autowired
    private OrderClient orderClient;

    /**
     * Lấy cart của customer
     */
    public CartResponse getCart() {

        Long customerId = getAuthenticatedCustomerId();

        Cart cart = cartRepository.findByCustomerId(customerId).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setCustomerId(customerId);
            return cartRepository.save(newCart);
        });

        // Force load details nếu LAZY
        cart.getDetails().size();

        return cartMapper.toResponseDTO(cart);
    }

    /**
     * Thêm item vào giỏ hàng
     */
    public void addItem(AddItemRequest requestDTO) {

        Long customerId = getAuthenticatedCustomerId();

        Cart cart = getOrCreateCart(customerId);

        Long variantId = requestDTO.getVariantId();
        int quantityToAdd = requestDTO.getQuantity();

        if (quantityToAdd <= 0) {
            throw new AppException(ErrorCode.INVALID_QUANTITY);
        }

        ApiResponse<VariantInfoResponse> response;

        try {
            response = productClient.getVariantById(variantId);

            if (response == null || response.getResult() == null) {
                throw new AppException(ErrorCode.VARIANT_NOT_FOUND);
            }

            VariantInfoResponse variant = response.getResult();

            if (!"ACTIVE".equals(variant.getStatus())) {
                throw new AppException(ErrorCode.VARIANT_NOT_FOUND);
            }

            if (quantityToAdd > variant.getStock()) {
                throw new AppException(ErrorCode.OUT_OF_STOCK);
            }

            CartDetail detail = cartDetailRepository
                    .findByCartIdAndVariantId(cart.getId(), variantId)
                    .orElse(null);

            if (detail == null) {
                detail = new CartDetail();
                detail.setCart(cart);
                detail.setVariantId(variantId);
                detail.setQuantity(quantityToAdd);
                detail.setPriceSnapshot(variant.getPrice());
            } else {
                int newQuantity = detail.getQuantity() + quantityToAdd;

                if (newQuantity > variant.getStock()) {
                    throw new AppException(ErrorCode.OUT_OF_STOCK);
                }

                detail.setQuantity(newQuantity);
            }

            cartDetailRepository.save(detail);

        } catch (Exception e) {
            log.error(e);
            throw new AppException(ErrorCode.PRODUCT_SERVICE_TIMEOUT);
        }
    }

    /**
     * Cập nhật số lượng item
     */
    public void updateQuantity(Long variantId, UpdateQuantityRequest requestDTO) {

        Long customerId = getAuthenticatedCustomerId();

        Cart cart = cartRepository
                .findByCustomerId(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        ApiResponse<VariantInfoResponse> response = productClient.getVariantById(variantId);

        if (response == null || response.getResult() == null) {
            throw new AppException(ErrorCode.VARIANT_NOT_FOUND);
        }

        VariantInfoResponse variant = response.getResult();

        if (!"ACTIVE".equals(variant.getStatus())) {
            throw new AppException(ErrorCode.VARIANT_NOT_FOUND);
        }

        if (requestDTO.getQuantity() < 0) {
            throw new AppException(ErrorCode.INVALID_QUANTITY);
        }

        if (requestDTO.getQuantity() > variant.getStock()) {
            throw new AppException(ErrorCode.OUT_OF_STOCK);
        }

        CartDetail detail = cartDetailRepository
                .findByCartIdAndVariantId(cart.getId(), variantId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        if (requestDTO.getQuantity() <= 0) {
            cartDetailRepository.delete(detail);
        } else {
            detail.setQuantity(requestDTO.getQuantity());
        }
    }

    /**
     * Xóa item khỏi giỏ hàng
     */
    public void removeItem(Long variantId) {

        Long customerId = getAuthenticatedCustomerId();

        Cart cart = cartRepository
                .findByCustomerId(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        CartDetail detail = cartDetailRepository
                .findByCartIdAndVariantId(cart.getId(), variantId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        cartDetailRepository.delete(detail);
    }

    /**
     * Checkout một phần giỏ hàng
     */
    public void checkout(CheckoutRequest requestDTO) {

        Long customerId = getAuthenticatedCustomerId();

        Cart cart = cartRepository
                .findByCustomerId(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        List<CartDetail> details =
                cartDetailRepository.findByIdInAndCartId(requestDTO.getCartDetailIds(), cart.getId());

        if (details.isEmpty()) {
            throw new AppException(ErrorCode.CART_ITEM_NOT_FOUND);
        }

        orderClient.createOrder(cartMapper.toCreateOrderRequest(customerId, details));

        cartDetailRepository.deleteAll(details);
    }

    /**
     * Lấy hoặc tạo cart
     */
    private Cart getOrCreateCart(Long customerId) {
        return cartRepository.findByCustomerId(customerId).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setCustomerId(customerId);
            return cartRepository.save(cart);
        });
    }

    private Long getAuthenticatedCustomerId() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();

        String userType = jwt.getClaim("user_type");

        if (!"CUSTOMER".equals(userType)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return Long.valueOf(jwt.getSubject());
    }
}
