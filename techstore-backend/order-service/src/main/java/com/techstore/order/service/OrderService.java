package com.techstore.order.service;

import com.techstore.order.dto.request.OrderCreateRequest;
import com.techstore.order.dto.response.OrderResponse;

public interface OrderService {

    OrderResponse createOrder(OrderCreateRequest request, String ipAddress);

    void confirmOrder(Long orderId, Long staffId);

    void cancelOrder(Long orderId, Long staffId);

    void updateStatus(Long orderId, String status);

    void createRefund(Long orderDetailId, String reason, Long staffId);

    public String printLabel(Long orderId);
}
