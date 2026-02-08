package com.techstore.cart.dto.request;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutRequest {
    private List<Long> cartDetailIds;
}
