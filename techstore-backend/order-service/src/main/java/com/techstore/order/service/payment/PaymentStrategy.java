package com.techstore.order.service.payment;

import java.util.Map;

import com.techstore.order.entity.Order;

public interface PaymentStrategy {

    String createPaymentUrl(Order order, String ipAddress);

    void handleCallback(Map<String, String> params);
}
