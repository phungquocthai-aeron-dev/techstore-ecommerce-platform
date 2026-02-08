package com.techstore.warehouse.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techstore.warehouse.constant.WarehouseStatus;
import com.techstore.warehouse.dto.request.SupplierCreateRequest;
import com.techstore.warehouse.dto.request.SupplierUpdateRequest;
import com.techstore.warehouse.dto.response.SupplierResponse;
import com.techstore.warehouse.entity.Supplier;
import com.techstore.warehouse.exception.AppException;
import com.techstore.warehouse.exception.ErrorCode;
import com.techstore.warehouse.mapper.SupplierMapper;
import com.techstore.warehouse.repository.SupplierRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupplierService {

    private final SupplierRepository supplierRepo;
    private final SupplierMapper supplierMapper;

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @Transactional
    public SupplierResponse create(SupplierCreateRequest req) {
        log.info("Creating supplier with phone: {}", req.getPhone());

        if (supplierRepo.findByPhone(req.getPhone()).isPresent()) {
            throw new AppException(ErrorCode.SUPPLIER_EXISTED);
        }

        Supplier supplier = supplierMapper.toEntity(req);
        supplier.setStatus(WarehouseStatus.ACTIVE.name());

        return supplierMapper.toResponse(supplierRepo.save(supplier));
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @Transactional
    public SupplierResponse update(Long id, SupplierUpdateRequest req) {
        log.info("Updating supplier with id: {}", id);

        Supplier supplier = supplierRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.SUPPLIER_NOT_FOUND));

        if (req.getPhone() != null && !req.getPhone().equals(supplier.getPhone())) {
            supplierRepo.findByPhone(req.getPhone()).ifPresent(s -> {
                throw new AppException(ErrorCode.SUPPLIER_EXISTED);
            });
        }

        supplierMapper.updateEntityFromRequest(req, supplier);
        return supplierMapper.toResponse(supplierRepo.save(supplier));
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public SupplierResponse getById(Long id) {
        return supplierMapper.toResponse(
                supplierRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.SUPPLIER_NOT_FOUND)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public SupplierResponse getByPhone(String phone) {
        return supplierMapper.toResponse(
                supplierRepo.findByPhone(phone).orElseThrow(() -> new AppException(ErrorCode.SUPPLIER_NOT_FOUND)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public List<SupplierResponse> getAll() {
        return supplierRepo.findAll().stream().map(supplierMapper::toResponse).toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public List<SupplierResponse> getByStatus(String status) {
        return supplierRepo.findByStatus(status).stream()
                .map(supplierMapper::toResponse)
                .toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public List<SupplierResponse> searchByName(String name) {
        return supplierRepo.findByNameContainingIgnoreCase(name).stream()
                .map(supplierMapper::toResponse)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(Long id) {
        log.info("Deleting supplier with id: {}", id);

        Supplier supplier = supplierRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.SUPPLIER_NOT_FOUND));

        supplierRepo.delete(supplier);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public SupplierResponse updateStatus(Long id, String status) {
        log.info("Updating supplier status for id: {} to status: {}", id, status);

        Supplier supplier = supplierRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.SUPPLIER_NOT_FOUND));

        supplier.setStatus(status);
        return supplierMapper.toResponse(supplierRepo.save(supplier));
    }
}
