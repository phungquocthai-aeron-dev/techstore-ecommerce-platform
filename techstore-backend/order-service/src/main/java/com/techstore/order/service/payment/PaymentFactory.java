package com.techstore.order.service.payment;

import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentFactory {

    private final Map<String, PaymentStrategy> strategies;

    public PaymentStrategy getStrategy(String method) {
        return strategies.get(method.toUpperCase());
    }
}
