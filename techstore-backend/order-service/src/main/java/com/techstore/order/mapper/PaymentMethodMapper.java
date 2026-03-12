package com.techstore.order.mapper;

import org.springframework.stereotype.Component;

import com.techstore.order.dto.response.PaymentMethodResponseDTO;
import com.techstore.order.entity.PaymentMethod;

@Component
public class PaymentMethodMapper {

    public PaymentMethodResponseDTO toResponseDTO(PaymentMethod paymentMethod) {
        return PaymentMethodResponseDTO.builder()
                .id(paymentMethod.getId())
                .name(paymentMethod.getName())
                .status(paymentMethod.getStatus())
                .build();
    }
}
