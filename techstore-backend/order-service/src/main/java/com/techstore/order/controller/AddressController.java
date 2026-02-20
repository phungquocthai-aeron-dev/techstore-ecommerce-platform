package com.techstore.order.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.techstore.order.dto.request.AddressRequest;
import com.techstore.order.dto.response.AddressResponse;
import com.techstore.order.dto.response.ApiResponse;
import com.techstore.order.service.AddressService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public ApiResponse<AddressResponse> create(@RequestParam Long customerId, @RequestBody AddressRequest request) {

        return ApiResponse.<AddressResponse>builder()
                .result(addressService.create(customerId, request))
                .build();
    }

    @GetMapping("/customer/{customerId}")
    public ApiResponse<List<AddressResponse>> getByCustomerId(@PathVariable Long customerId) {

        return ApiResponse.<List<AddressResponse>>builder()
                .result(addressService.getByCustomerId(customerId))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<AddressResponse> update(@PathVariable Long id, @RequestBody AddressRequest request) {

        return ApiResponse.<AddressResponse>builder()
                .result(addressService.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {

        addressService.delete(id);
        return ApiResponse.<Void>builder().build();
    }
}
