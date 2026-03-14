package com.techstore.order.service;

import java.util.List;

import com.techstore.order.dto.request.OrderCreateRequest;
import com.techstore.order.dto.response.AdminOrderResponse;
import com.techstore.order.dto.response.CustomerOrderResponse;
import com.techstore.order.dto.response.OrderDetailResponse;
import com.techstore.order.dto.response.OrderResponse;

public interface OrderService {

    List<CustomerOrderResponse> getOrdersByCustomer(Long customerId, String status);

    List<AdminOrderResponse> getAllOrders(String status);

    OrderResponse createOrder(OrderCreateRequest request, String ipAddress);

    void confirmOrder(Long orderId, Long staffId);

    void cancelOrder(Long orderId, Long staffId);

    void updateStatus(Long orderId, String status);

    void createRefund(Long orderDetailId, String reason, Long staffId);

    public String printLabel(Long orderId);

    public OrderDetailResponse getOrderDetail(Long detailId);

    void markOrderDetailReviewed(Long orderDetailId);
}
