package com.techstore.order.service.shipping.impl;

import org.springframework.stereotype.Service;

import com.techstore.order.service.shipping.ShippingService;

@Service("GHN")
public class GHNShippingService implements ShippingService {

    @Override
    public Double calculateFee(Long addressId, Double weight) {
        // gọi API GHN thật ở đây
        return 30000.0;
    }
}
