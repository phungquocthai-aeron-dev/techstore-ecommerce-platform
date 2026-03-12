package com.techstore.order.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentMethodResponseDTO {

    private Long id;
    private String name;
    private String status;
}
