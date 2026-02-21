package com.techstore.order.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.*;

import com.techstore.order.dto.request.OrderCreateRequest;
import com.techstore.order.dto.response.ApiResponse;
import com.techstore.order.dto.response.OrderDetailResponse;
import com.techstore.order.dto.response.OrderResponse;
import com.techstore.order.service.OrderService;
import com.techstore.order.util.VNPayUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ApiResponse<OrderResponse> createOrder(
            @RequestBody OrderCreateRequest request, HttpServletRequest servletRequest) {

        String ipAddress = VNPayUtils.getIpAddress(servletRequest);

        return ApiResponse.<OrderResponse>builder()
                .result(orderService.createOrder(request, ipAddress))
                .build();
    }

    @GetMapping("/order-detail/{id}")
    public ApiResponse<OrderDetailResponse> getOrderDetail(@PathVariable Long id) {
        return ApiResponse.<OrderDetailResponse>builder()
                .result(orderService.getOrderDetail(id))
                .build();
    }

    @PostMapping("/{id}/confirm")
    public ApiResponse<Void> confirmOrder(@PathVariable Long id, @RequestParam Long staffId) {

        orderService.confirmOrder(id, staffId);

        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancelOrder(@PathVariable Long id, @RequestParam Long staffId) {

        orderService.cancelOrder(id, staffId);

        return ApiResponse.<Void>builder().build();
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {

        orderService.updateStatus(id, status);

        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/{id}/print-label")
    public ApiResponse<String> printLabel(@PathVariable Long id) {
        return ApiResponse.<String>builder().result(orderService.printLabel(id)).build();
    }
}
