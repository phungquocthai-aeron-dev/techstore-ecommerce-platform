package com.techstore.cart.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.techstore.cart.response.CartResponse;
import com.techstore.cart.response.VariantInfoResponse;

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
    @Transactional(readOnly = true)
    public CartResponse getCart(Long customerId) {
        Cart cart = cartRepository
                .findByCustomerId(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        return cartMapper.toResponseDTO(cart);
    }

    /**
     * Thêm item vào giỏ hàng
     */
    public void addItem(Long customerId, AddItemRequest requestDTO) {

        Cart cart = getOrCreateCart(customerId);

        CartDetail detail = cartDetailRepository
                .findByCartIdAndVariantId(cart.getId(), requestDTO.getVariantId())
                .orElseGet(() -> {
                    CartDetail d = new CartDetail();
                    d.setCart(cart);
                    d.setVariantId(requestDTO.getVariantId());
                    d.setQuantity(0);

                    VariantInfoResponse variant;
                    try {
                        variant = productClient.getVariantInfo(requestDTO.getVariantId());
                    } catch (Exception e) {
                        throw new AppException(ErrorCode.VARIANT_NOT_FOUND);
                    }

                    d.setPriceSnapshot(variant.getPrice());
                    return d;
                });

        detail.setQuantity(detail.getQuantity() + requestDTO.getQuantity());
        cartDetailRepository.save(detail);
    }

    /**
     * Cập nhật số lượng item
     */
    public void updateQuantity(Long customerId, Long variantId, UpdateQuantityRequest requestDTO) {

        Cart cart = getOrCreateCart(customerId);

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
    public void removeItem(Long customerId, Long variantId) {

        Cart cart = getOrCreateCart(customerId);

        CartDetail detail = cartDetailRepository
                .findByCartIdAndVariantId(cart.getId(), variantId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        cartDetailRepository.delete(detail);
    }

    /**
     * Checkout một phần giỏ hàng
     */
    public void checkout(Long customerId, CheckoutRequest requestDTO) {

        Cart cart = getOrCreateCart(customerId);

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
}
