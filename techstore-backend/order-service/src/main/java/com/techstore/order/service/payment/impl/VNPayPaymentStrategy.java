package com.techstore.order.service.payment.impl;

import java.util.Map;
import java.util.UUID;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.techstore.order.entity.Order;
import com.techstore.order.entity.Payment;
import com.techstore.order.repository.PaymentRepository;
import com.techstore.order.service.payment.PaymentStrategy;
import com.techstore.order.util.VNPayUtils;

import lombok.RequiredArgsConstructor;

@Service("VNPAY")
@RequiredArgsConstructor
public class VNPayPaymentStrategy implements PaymentStrategy {

    private final PaymentRepository paymentRepository;

    @Override
    public String createPaymentUrl(Order order, String ipAddress) {

        String txnRef = UUID.randomUUID().toString().replace("-", "");
        long amount = order.getTotalPrice().longValue() * 100;

        Map<String, String> params =
                VNPayUtils.buildBaseParams(txnRef, amount, "Thanh toan don hang " + order.getId(), ipAddress);

        String paymentUrl = VNPayUtils.buildPaymentUrl(params);

        Payment payment = Payment.builder()
                .transactionCode(txnRef)
                .status("PENDING")
                .order(order)
                .build();

        paymentRepository.save(payment);

        return paymentUrl;
    }

    @Transactional
    public void handleCallback(Map<String, String> params) {

        if (!VNPayUtils.validateSignature(params)) {
            throw new RuntimeException("Invalid signature");
        }

        String txnRef = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        long vnpAmount = Long.parseLong(params.get("vnp_Amount")) / 100;

        Payment payment = paymentRepository
                .findByTransactionCode(txnRef)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        Order order = payment.getOrder();

        // Idempotent check
        if ("SUCCESS".equals(payment.getStatus())) {
            return;
        }

        // Amount check
        if (order.getTotalPrice().longValue() != vnpAmount) {
            payment.setStatus("FAILED");
            throw new RuntimeException("Amount mismatch");
        }

        if ("00".equals(responseCode)) {
            payment.setStatus("SUCCESS");
            order.setStatus("PROCESSING");
        } else {
            payment.setStatus("FAILED");
        }
    }
}
