package com.techstore.order.service.shipping.impl;

import org.springframework.stereotype.Service;

import com.techstore.order.service.shipping.ShippingService;

@Service("GHTK")
public class GHTKShippingService implements ShippingService {

    @Override
    public Double calculateFee(Long addressId, Double weight) {
        return 35000.0;
    }
}
