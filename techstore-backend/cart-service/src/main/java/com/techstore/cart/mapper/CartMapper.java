package com.techstore.cart.mapper;

import java.math.BigDecimal;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.techstore.cart.dto.request.CreateOrderItemRequest;
import com.techstore.cart.dto.request.CreateOrderRequest;
import com.techstore.cart.entity.Cart;
import com.techstore.cart.entity.CartDetail;
import com.techstore.cart.response.CartResponse;

@Mapper(componentModel = "spring", uses = CartItemMapper.class, imports = BigDecimal.class)
public interface CartMapper {

    @Mapping(target = "cartId", source = "id")
    @Mapping(target = "items", source = "details")
    @Mapping(target = "totalPrice", expression = "java(BigDecimal.valueOf(calculateTotal(cart)))")
    CartResponse toResponseDTO(Cart cart);

    default Double calculateTotal(Cart cart) {
        if (cart.getDetails() == null || cart.getDetails().isEmpty()) {
            return 0.0;
        }

        return cart.getDetails().stream()
                .mapToDouble(d -> d.getPriceSnapshot() * d.getQuantity())
                .sum();
    }

    default CreateOrderRequest toCreateOrderRequest(Long customerId, List<CartDetail> details) {

        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(customerId);

        request.setItems(details.stream().map(this::toCreateOrderItem).toList());

        BigDecimal totalAmount = details.stream()
                .map(d -> BigDecimal.valueOf(d.getPriceSnapshot()).multiply(BigDecimal.valueOf(d.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        request.setTotalAmount(totalAmount);
        return request;
    }

    default CreateOrderItemRequest toCreateOrderItem(CartDetail detail) {

        CreateOrderItemRequest item = new CreateOrderItemRequest();
        item.setVariantId(detail.getVariantId());
        item.setQuantity(detail.getQuantity());
        item.setPrice(BigDecimal.valueOf(detail.getPriceSnapshot()));

        return item;
    }
}
