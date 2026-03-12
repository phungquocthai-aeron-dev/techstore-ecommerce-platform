package com.techstore.order.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentMethodCreateRequestDTO {

    private String name;
    private String status;
}
