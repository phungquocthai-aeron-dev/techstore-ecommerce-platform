package com.techstore.order.service.shipping.impl;

import org.springframework.stereotype.Service;

import com.techstore.order.service.shipping.ShippingService;

@Service("NinjaVan")
public class NinjaVanShippingService implements ShippingService {

    @Override
    public Double calculateFee(Long addressId, Double weight) {
        return 38000.0;
    }
}
