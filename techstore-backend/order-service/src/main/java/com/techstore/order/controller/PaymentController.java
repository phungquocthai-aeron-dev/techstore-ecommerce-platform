package com.techstore.order.controller;

import java.io.IOException;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
    public void handleVNPayIPN(
            @RequestParam Map<String, String> allParams, HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        PaymentStrategy strategy = paymentFactory.getStrategy("VNPAY");

        strategy.handleCallback(allParams);

        response.sendRedirect("http://localhost:4200/order-success?txnRef=" + allParams.get("vnp_TxnRef"));
    }
}
