package com.techstore.order.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentMethodSearchRequestDTO {

    private String keyword;
    private String status;

    private int page = 0;
    private int size = 10;

    private String sortBy = "id";
    private String sortDirection = "DESC";
}
