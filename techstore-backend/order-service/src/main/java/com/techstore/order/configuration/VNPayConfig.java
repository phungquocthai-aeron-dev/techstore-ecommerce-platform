package com.techstore.order.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VNPayConfig {

    public static String vnp_PayUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    public static String vnp_ReturnUrl = "http://localhost:8888/techstore/api/v1/order/payment/vnpay/ipn";

    public static String vnp_TmnCode;
    public static String secretKey;

    @Value("${VNP_TMN_CODE:your-tmnCode}")
    public void setTmnCode(String tmnCode) {
        VNPayConfig.vnp_TmnCode = tmnCode;
    }

    @Value("${VNP_SECRET_KEY:your-secretKey}")
    public void setSecretKey(String key) {
        VNPayConfig.secretKey = key;
    }
}