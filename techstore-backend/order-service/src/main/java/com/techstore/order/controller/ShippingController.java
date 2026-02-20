package com.techstore.order.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.techstore.order.dto.response.ApiResponse;
import com.techstore.order.dto.response.GHNDistrictResponse.DistrictData;
import com.techstore.order.dto.response.GHNProvinceResponse.ProvinceData;
import com.techstore.order.dto.response.GHNWardResponse.WardData;
import com.techstore.order.service.shipping.ShippingFactory;
import com.techstore.order.service.shipping.ShippingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/shipping")
@RequiredArgsConstructor
public class ShippingController {

    private final ShippingFactory shippingFactory;

    @GetMapping("/{type}/provinces")
    public ApiResponse<List<ProvinceData>> getProvinces(@PathVariable String type) {

        ShippingService service = shippingFactory.getService(type);

        return ApiResponse.<List<ProvinceData>>builder()
                .result(service.getProvinces())
                .build();
    }

    @GetMapping("/{type}/districts")
    public ApiResponse<List<DistrictData>> getDistricts(@PathVariable String type, @RequestParam Long provinceId) {

        ShippingService service = shippingFactory.getService(type);

        return ApiResponse.<List<DistrictData>>builder()
                .result(service.getDistricts(provinceId))
                .build();
    }

    @GetMapping("/{type}/wards")
    public ApiResponse<List<WardData>> getWards(@PathVariable String type, @RequestParam Long districtId) {

        ShippingService service = shippingFactory.getService(type);

        return ApiResponse.<List<WardData>>builder()
                .result(service.getWards(districtId))
                .build();
    }

    @GetMapping("/{type}/fee")
    public ApiResponse<Double> calculateFee(
            @PathVariable String type, @RequestParam Long addressId, @RequestParam Double weight) {

        ShippingService service = shippingFactory.getService(type);

        return ApiResponse.<Double>builder()
                .result(service.calculateFee(addressId, weight))
                .build();
    }
}
