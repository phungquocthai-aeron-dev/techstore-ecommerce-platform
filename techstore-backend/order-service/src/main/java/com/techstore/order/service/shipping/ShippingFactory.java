package com.techstore.order.service.shipping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.techstore.order.exception.AppException;
import com.techstore.order.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ShippingFactory {

    private final List<ShippingService> services;

    private Map<String, ShippingService> serviceMap;

    @PostConstruct
    public void init() {
        serviceMap = services.stream().collect(Collectors.toMap(s -> s.getType().toLowerCase(), s -> s));
    }

    public ShippingService getService(String type) {

        if (type == null || type.isBlank()) {
            throw new AppException(ErrorCode.INVALID_SHIPPING_TYPE);
        }

        ShippingService service = serviceMap.get(type.toLowerCase());

        if (service == null) {
            throw new AppException(ErrorCode.UNSUPPORTED_SHIPPING_TYPE);
        }

        return service;
    }
}
