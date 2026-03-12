package com.techstore.order.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techstore.order.dto.request.PaymentMethodCreateRequestDTO;
import com.techstore.order.dto.request.PaymentMethodSearchRequestDTO;
import com.techstore.order.dto.request.PaymentMethodStatusUpdateRequestDTO;
import com.techstore.order.dto.request.PaymentMethodUpdateRequestDTO;
import com.techstore.order.dto.response.PageResponseDTO;
import com.techstore.order.dto.response.PaymentMethodResponseDTO;
import com.techstore.order.entity.PaymentMethod;
import com.techstore.order.exception.AppException;
import com.techstore.order.exception.ErrorCode;
import com.techstore.order.mapper.PaymentMethodMapper;
import com.techstore.order.repository.PaymentMethodRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentMethodMapper paymentMethodMapper;

    @PreAuthorize("hasRole('ADMIN')")
    public PaymentMethodResponseDTO createPaymentMethod(PaymentMethodCreateRequestDTO requestDTO) {

        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setName(requestDTO.getName());
        paymentMethod.setStatus(requestDTO.getStatus());

        PaymentMethod saved = paymentMethodRepository.save(paymentMethod);
        return paymentMethodMapper.toResponseDTO(saved);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public PaymentMethodResponseDTO updatePaymentMethod(Long id, PaymentMethodUpdateRequestDTO requestDTO) {

        PaymentMethod paymentMethod = paymentMethodRepository
                .findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_METHOD_NOT_FOUND));

        if (requestDTO.getName() != null) {
            paymentMethod.setName(requestDTO.getName());
        }

        if (requestDTO.getStatus() != null) {
            paymentMethod.setStatus(requestDTO.getStatus());
        }

        PaymentMethod updated = paymentMethodRepository.save(paymentMethod);

        return paymentMethodMapper.toResponseDTO(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public PaymentMethodResponseDTO updatePaymentMethodStatus(Long id, PaymentMethodStatusUpdateRequestDTO requestDTO) {

        PaymentMethod paymentMethod = paymentMethodRepository
                .findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_METHOD_NOT_FOUND));

        paymentMethod.setStatus(requestDTO.getStatus());

        PaymentMethod updated = paymentMethodRepository.save(paymentMethod);

        return paymentMethodMapper.toResponseDTO(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deletePaymentMethod(Long id) {

        PaymentMethod paymentMethod = paymentMethodRepository
                .findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_METHOD_NOT_FOUND));

        paymentMethodRepository.delete(paymentMethod);
    }

    @Transactional(readOnly = true)
    public PaymentMethodResponseDTO getPaymentMethodById(Long id) {

        PaymentMethod paymentMethod = paymentMethodRepository
                .findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_METHOD_NOT_FOUND));

        return paymentMethodMapper.toResponseDTO(paymentMethod);
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<PaymentMethodResponseDTO> getAllPaymentMethods(
            int page, int size, String sortBy, String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<PaymentMethod> paymentPage = paymentMethodRepository.findAll(pageable);

        List<PaymentMethodResponseDTO> content = paymentPage.getContent().stream()
                .map(paymentMethodMapper::toResponseDTO)
                .toList();

        return new PageResponseDTO<>(
                content,
                paymentPage.getNumber(),
                paymentPage.getSize(),
                paymentPage.getTotalElements(),
                paymentPage.getTotalPages(),
                paymentPage.isLast());
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<PaymentMethodResponseDTO> searchPaymentMethods(PaymentMethodSearchRequestDTO searchDTO) {

        Sort sort = searchDTO.getSortDirection().equalsIgnoreCase("ASC")
                ? Sort.by(searchDTO.getSortBy()).ascending()
                : Sort.by(searchDTO.getSortBy()).descending();

        Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize(), sort);

        Page<PaymentMethod> paymentPage =
                paymentMethodRepository.searchPaymentMethods(searchDTO.getKeyword(), searchDTO.getStatus(), pageable);

        List<PaymentMethodResponseDTO> content = paymentPage.getContent().stream()
                .map(paymentMethodMapper::toResponseDTO)
                .toList();

        return new PageResponseDTO<>(
                content,
                paymentPage.getNumber(),
                paymentPage.getSize(),
                paymentPage.getTotalElements(),
                paymentPage.getTotalPages(),
                paymentPage.isLast());
    }
}
