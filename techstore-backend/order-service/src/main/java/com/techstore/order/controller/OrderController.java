package com.techstore.order.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.*;

import com.techstore.order.request.OrderCreateRequest;
import com.techstore.order.request.OrderResponse;
import com.techstore.order.service.OrderService;
import com.techstore.order.util.VNPayUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public OrderResponse createOrder(@RequestBody OrderCreateRequest request, HttpServletRequest servletRequest) {

        String ipAddress = VNPayUtils.getIpAddress(servletRequest);

        return orderService.createOrder(request, ipAddress);
    }

    @PostMapping("/{id}/confirm")
    public void confirmOrder(@PathVariable Long id, @RequestParam Long staffId) {
        orderService.confirmOrder(id, staffId);
    }

    @PostMapping("/{id}/cancel")
    public void cancelOrder(@PathVariable Long id, @RequestParam Long staffId) {
        orderService.cancelOrder(id, staffId);
    }

    @PutMapping("/{id}/status")
    public void updateStatus(@PathVariable Long id, @RequestParam String status) {
        orderService.updateStatus(id, status);
    }
}
