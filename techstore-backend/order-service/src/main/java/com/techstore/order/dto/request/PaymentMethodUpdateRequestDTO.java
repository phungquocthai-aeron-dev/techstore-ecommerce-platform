package com.techstore.order.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentMethodUpdateRequestDTO {

    private String name;
    private String status;
}
