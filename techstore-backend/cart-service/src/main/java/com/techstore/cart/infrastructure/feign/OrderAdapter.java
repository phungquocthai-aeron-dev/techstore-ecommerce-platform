package com.techstore.cart.infrastructure.feign;

import com.techstore.cart.domain.model.CartItem;
import com.techstore.cart.domain.service.OrderPort;
import com.techstore.cart.infrastructure.feign.client.OrderFeignClient;
import com.techstore.cart.infrastructure.feign.dto.CreateOrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderAdapter implements OrderPort {

    private final OrderFeignClient orderFeignClient;

    @Override
    public void createOrder(Long customerId, List<CartItem> items) {
        CreateOrderRequest request = buildRequest(customerId, items);
        try {
            orderFeignClient.createOrder(request);
        } catch (Exception e) {
            log.error("Failed to create order for customer {}: {}", customerId, e.getMessage());
            throw new com.techstore.cart.infrastructure.feign.exception.OrderServiceException(
                    "Order service unavailable", e);
        }
    }

    private CreateOrderRequest buildRequest(Long customerId, List<CartItem> items) {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(customerId);

        List<CreateOrderRequest.CreateOrderItemRequest> orderItems = items.stream().map(i -> {
            CreateOrderRequest.CreateOrderItemRequest item = new CreateOrderRequest.CreateOrderItemRequest();
            item.setVariantId(i.getVariantId());
            item.setQuantity(i.getQuantity());
            item.setPrice(i.getPriceSnapshot());
            return item;
        }).toList();

        request.setItems(orderItems);
        request.setTotalAmount(items.stream()
                .map(CartItem::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        return request;
    }
}
