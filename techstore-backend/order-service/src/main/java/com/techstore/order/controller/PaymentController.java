package com.techstore.order.controller;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.*;

import com.techstore.order.service.payment.PaymentStrategy;
import com.techstore.order.service.payment.PaymentStrategyFactory;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentStrategyFactory paymentFactory;

    @GetMapping("/vnpay/ipn")
    public Map<String, String> handleVNPayIPN(@RequestParam Map<String, String> allParams, HttpServletRequest request) {

        PaymentStrategy strategy = paymentFactory.getStrategy("VNPAY");

        strategy.handleCallback(allParams);

        Map<String, String> response = new HashMap<>();
        response.put("RspCode", "00");
        response.put("Message", "Confirm Success");

        return response;
    }
}
