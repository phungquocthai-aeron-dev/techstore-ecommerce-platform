package com.techstore.order.service.shipping;

public interface ShippingService {
    Double calculateFee(Long addressId, Double weight);
}
