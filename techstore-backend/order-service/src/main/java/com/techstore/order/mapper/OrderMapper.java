package com.techstore.order.mapper;

import org.springframework.stereotype.Component;

import com.techstore.order.dto.response.OrderResponse;
import com.techstore.order.entity.Order;

@Component
public class OrderMapper {

    public OrderResponse toDto(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .totalPrice(order.getTotalPrice())
                .shippingFee(order.getShippingFee())
                .vat(order.getVat())
                .status(order.getStatus())
                .build();
    }
}
