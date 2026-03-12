package com.techstore.order.controller;

import org.springframework.web.bind.annotation.*;

import com.techstore.order.dto.request.PaymentMethodCreateRequestDTO;
import com.techstore.order.dto.request.PaymentMethodSearchRequestDTO;
import com.techstore.order.dto.request.PaymentMethodStatusUpdateRequestDTO;
import com.techstore.order.dto.request.PaymentMethodUpdateRequestDTO;
import com.techstore.order.dto.response.ApiResponse;
import com.techstore.order.dto.response.PageResponseDTO;
import com.techstore.order.dto.response.PaymentMethodResponseDTO;
import com.techstore.order.service.PaymentMethodService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/payment-methods")
@RequiredArgsConstructor
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    @PostMapping
    public ApiResponse<PaymentMethodResponseDTO> createPaymentMethod(
            @RequestBody PaymentMethodCreateRequestDTO requestDTO) {

        return ApiResponse.<PaymentMethodResponseDTO>builder()
                .result(paymentMethodService.createPaymentMethod(requestDTO))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<PaymentMethodResponseDTO> updatePaymentMethod(
            @PathVariable Long id, @RequestBody PaymentMethodUpdateRequestDTO requestDTO) {

        return ApiResponse.<PaymentMethodResponseDTO>builder()
                .result(paymentMethodService.updatePaymentMethod(id, requestDTO))
                .build();
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<PaymentMethodResponseDTO> updateStatus(
            @PathVariable Long id, @RequestBody PaymentMethodStatusUpdateRequestDTO requestDTO) {

        return ApiResponse.<PaymentMethodResponseDTO>builder()
                .result(paymentMethodService.updatePaymentMethodStatus(id, requestDTO))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePaymentMethod(@PathVariable Long id) {

        paymentMethodService.deletePaymentMethod(id);

        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/{id}")
    public ApiResponse<PaymentMethodResponseDTO> getById(@PathVariable Long id) {

        return ApiResponse.<PaymentMethodResponseDTO>builder()
                .result(paymentMethodService.getPaymentMethodById(id))
                .build();
    }

    @GetMapping
    public ApiResponse<PageResponseDTO<PaymentMethodResponseDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        return ApiResponse.<PageResponseDTO<PaymentMethodResponseDTO>>builder()
                .result(paymentMethodService.getAllPaymentMethods(page, size, sortBy, sortDirection))
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<PageResponseDTO<PaymentMethodResponseDTO>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        PaymentMethodSearchRequestDTO dto = new PaymentMethodSearchRequestDTO();

        dto.setKeyword(keyword);
        dto.setStatus(status);
        dto.setPage(page);
        dto.setSize(size);
        dto.setSortBy(sortBy);
        dto.setSortDirection(sortDirection);

        return ApiResponse.<PageResponseDTO<PaymentMethodResponseDTO>>builder()
                .result(paymentMethodService.searchPaymentMethods(dto))
                .build();
    }
}
