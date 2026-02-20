package com.techstore.order.configuration;

import org.springframework.context.annotation.Configuration;

@Configuration
public class VNPayConfig {

    public static String vnp_PayUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    public static String vnp_ReturnUrl = "http://localhost:8888/techstore/api/v1/order/payment/vnpay/ipn";
    public static String vnp_TmnCode = "I3O6DXQ5";
    public static String secretKey = "P7KIVQL09ZJGEORFN3OAEOERNOO3KCVT";
}
