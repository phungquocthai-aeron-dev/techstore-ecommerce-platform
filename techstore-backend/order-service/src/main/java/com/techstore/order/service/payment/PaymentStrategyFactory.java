package com.techstore.order.service.payment;

import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentStrategyFactory {

    private final Map<String, PaymentStrategy> strategies;

    public PaymentStrategy getStrategy(String method) {
        PaymentStrategy strategy = strategies.get(method);

        if (strategy == null) {
            throw new RuntimeException("Unsupported payment method: " + method);
        }

        return strategy;
    }
}
