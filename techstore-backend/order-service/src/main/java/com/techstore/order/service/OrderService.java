package com.techstore.order.service;

import java.time.LocalDate;
import java.util.List;

import com.techstore.order.dto.request.OrderCreateRequest;
import com.techstore.order.dto.response.AdminOrderResponse;
import com.techstore.order.dto.response.CustomerOrderResponse;
import com.techstore.order.dto.response.OrderDetailResponse;
import com.techstore.order.dto.response.OrderResponse;
import com.techstore.order.dto.response.OrderSummaryResponse;
import com.techstore.order.dto.response.ProductSalesResponse;
import com.techstore.order.dto.response.RevenueStatsResponse;
import com.techstore.order.dto.response.TopLoyalCustomerResponse;
import com.techstore.order.dto.response.TopVariantResponse;

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

    RevenueStatsResponse getRevenueStats(String period, LocalDate from, LocalDate to);

    List<TopVariantResponse> getTopVariants(int top, LocalDate from, LocalDate to);

    OrderSummaryResponse getOrderSummary(String status, LocalDate from, LocalDate to);

    List<TopLoyalCustomerResponse> getTopLoyalCustomers(int top, String period, LocalDate from, LocalDate to);

    ProductSalesResponse getProductSales(Long productId, String period, LocalDate from, LocalDate to);

    void markOrderDetailReviewed(Long orderDetailId);
}
