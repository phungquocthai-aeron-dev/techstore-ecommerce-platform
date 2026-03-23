package com.techstore.warehouse.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techstore.warehouse.constant.WarehouseStatus;
import com.techstore.warehouse.dto.request.WarehouseCreateRequest;
import com.techstore.warehouse.dto.request.WarehouseUpdateRequest;
import com.techstore.warehouse.dto.response.WarehouseResponse;
import com.techstore.warehouse.entity.Warehouse;
import com.techstore.warehouse.exception.AppException;
import com.techstore.warehouse.exception.ErrorCode;
import com.techstore.warehouse.mapper.WarehouseMapper;
import com.techstore.warehouse.repository.WarehouseRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseService {

    private final WarehouseRepository warehouseRepo;
    private final WarehouseMapper warehouseMapper;

    @PreAuthorize("hasAnyRole('ADMIN')")
    @Transactional
    public WarehouseResponse create(WarehouseCreateRequest req) {
        log.info("Creating warehouse with name: {}", req.getName());

        if (warehouseRepo.findByName(req.getName()).isPresent()) {
            throw new AppException(ErrorCode.WAREHOUSE_EXISTED);
        }

        Warehouse warehouse = warehouseMapper.toEntity(req);
        warehouse.setStatus(WarehouseStatus.ACTIVE.name());

        return warehouseMapper.toResponse(warehouseRepo.save(warehouse));
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @Transactional
    public WarehouseResponse update(Long id, WarehouseUpdateRequest req) {
        log.info("Updating warehouse with id: {}", id);

        Warehouse warehouse =
                warehouseRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));

        if (req.getName() != null && !req.getName().equals(warehouse.getName())) {
            warehouseRepo.findByName(req.getName()).ifPresent(w -> {
                throw new AppException(ErrorCode.WAREHOUSE_EXISTED);
            });
        }

        warehouseMapper.updateEntityFromRequest(req, warehouse);
        return warehouseMapper.toResponse(warehouseRepo.save(warehouse));
    }

    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_STAFF')")
    public WarehouseResponse getById(Long id) {
        return warehouseMapper.toResponse(
                warehouseRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_STAFF')")
    public WarehouseResponse getByName(String name) {
        return warehouseMapper.toResponse(
                warehouseRepo.findByName(name).orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_STAFF')")
    public List<WarehouseResponse> getAll() {
        return warehouseRepo.findAll().stream().map(warehouseMapper::toResponse).toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_STAFF')")
    public List<WarehouseResponse> getByStatus(String status) {
        return warehouseRepo.findByStatus(status).stream()
                .map(warehouseMapper::toResponse)
                .toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_STAFF')")
    public List<WarehouseResponse> getByAddress(String addressId) {
        return warehouseRepo.findByAddressId(addressId).stream()
                .map(warehouseMapper::toResponse)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(Long id) {
        log.info("Deleting warehouse with id: {}", id);

        Warehouse warehouse =
                warehouseRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));

        warehouseRepo.delete(warehouse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public WarehouseResponse updateStatus(Long id, String status) {
        log.info("Updating warehouse status for id: {} to status: {}", id, status);

        Warehouse warehouse =
                warehouseRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));

        warehouse.setStatus(status);
        return warehouseMapper.toResponse(warehouseRepo.save(warehouse));
    }
}
