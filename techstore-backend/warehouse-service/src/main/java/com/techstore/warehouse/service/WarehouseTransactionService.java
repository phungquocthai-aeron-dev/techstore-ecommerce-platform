package com.techstore.warehouse.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techstore.warehouse.client.UserServiceClient;
import com.techstore.warehouse.constant.TransactionStatus;
import com.techstore.warehouse.constant.TransactionType;
import com.techstore.warehouse.dto.request.TransactionDetailRequest;
import com.techstore.warehouse.dto.request.WarehouseTransactionCreateRequest;
import com.techstore.warehouse.dto.response.WarehouseTransactionResponse;
import com.techstore.warehouse.entity.Inventory;
import com.techstore.warehouse.entity.Supplier;
import com.techstore.warehouse.entity.Warehouse;
import com.techstore.warehouse.entity.WarehouseTransaction;
import com.techstore.warehouse.entity.WarehouseTransactionDetail;
import com.techstore.warehouse.exception.AppException;
import com.techstore.warehouse.exception.ErrorCode;
import com.techstore.warehouse.mapper.WarehouseTransactionMapper;
import com.techstore.warehouse.repository.SupplierRepository;
import com.techstore.warehouse.repository.WarehouseRepository;
import com.techstore.warehouse.repository.WarehouseTransactionRepository;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseTransactionService {

    private final WarehouseTransactionRepository transactionRepo;
    private final WarehouseRepository warehouseRepo;
    private final SupplierRepository supplierRepo;
    private final WarehouseTransactionMapper transactionMapper;
    private final InventoryService inventoryService;
    private final UserServiceClient userClient;

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @Transactional
    public WarehouseTransactionResponse createInboundTransaction(WarehouseTransactionCreateRequest req) {
        log.info("Creating INBOUND transaction for warehouse: {}", req.getWarehouseId());

        validateTransactionType(req.getTransactionType(), TransactionType.INBOUND.name());

        Warehouse warehouse = warehouseRepo
                .findById(req.getWarehouseId())
                .orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));

        Supplier supplier = null;
        if (req.getSupplierId() != null) {
            supplier = supplierRepo
                    .findById(req.getSupplierId())
                    .orElseThrow(() -> new AppException(ErrorCode.SUPPLIER_NOT_FOUND));
        }

        // Verify staff exists
        verifyStaffExists(req.getStaffId());

        // Create transaction
        WarehouseTransaction transaction = WarehouseTransaction.builder()
                .warehouse(warehouse)
                .supplier(supplier)
                .note(req.getNote())
                .transactionType(TransactionType.INBOUND.name())
                .referenceType(req.getReferenceType())
                .orderId(req.getOrderId())
                .staffId(req.getStaffId())
                .status(TransactionStatus.PENDING.name())
                .build();

        List<WarehouseTransactionDetail> details = new ArrayList<>();

        for (TransactionDetailRequest detailReq : req.getDetails()) {
            // Create or update inventory
            Inventory inventory = inventoryService.createOrUpdateInventory(
                    warehouse.getId(),
                    detailReq.getVariantId(),
                    detailReq.getQuantity(),
                    detailReq.getBatchCode() != null ? detailReq.getBatchCode() : "DEFAULT");

            // Create transaction detail
            WarehouseTransactionDetail detail = WarehouseTransactionDetail.builder()
                    .transaction(transaction)
                    .inventory(inventory)
                    .variantId(detailReq.getVariantId())
                    .quantity(detailReq.getQuantity())
                    .build();

            details.add(detail);
        }

        transaction.setDetails(details);
        transaction.setStatus(TransactionStatus.COMPLETED.name());

        WarehouseTransaction saved = transactionRepo.save(transaction);
        return transactionMapper.toResponse(saved);
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @Transactional
    public WarehouseTransactionResponse createOutboundTransaction(WarehouseTransactionCreateRequest req) {
        log.info("Creating OUTBOUND transaction for warehouse: {}", req.getWarehouseId());

        validateTransactionType(req.getTransactionType(), TransactionType.OUTBOUND.name());

        Warehouse warehouse = warehouseRepo
                .findById(req.getWarehouseId())
                .orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));

        // Verify staff exists
        verifyStaffExists(req.getStaffId());

        // Create transaction
        WarehouseTransaction transaction = WarehouseTransaction.builder()
                .warehouse(warehouse)
                .note(req.getNote())
                .transactionType(TransactionType.OUTBOUND.name())
                .referenceType(req.getReferenceType())
                .orderId(req.getOrderId())
                .staffId(req.getStaffId())
                .status(TransactionStatus.PENDING.name())
                .build();

        List<WarehouseTransactionDetail> details = new ArrayList<>();

        for (TransactionDetailRequest detailReq : req.getDetails()) {
            // Find available inventory
            Inventory inventory = inventoryService.findAvailableInventory(
                    warehouse.getId(), detailReq.getVariantId(), detailReq.getQuantity());

            // Reduce inventory
            inventoryService.reduceInventory(inventory.getId(), detailReq.getQuantity());

            // Create transaction detail
            WarehouseTransactionDetail detail = WarehouseTransactionDetail.builder()
                    .transaction(transaction)
                    .inventory(inventory)
                    .variantId(detailReq.getVariantId())
                    .quantity(detailReq.getQuantity())
                    .build();

            details.add(detail);
        }

        transaction.setDetails(details);
        transaction.setStatus(TransactionStatus.COMPLETED.name());

        WarehouseTransaction saved = transactionRepo.save(transaction);
        return transactionMapper.toResponse(saved);
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public WarehouseTransactionResponse getById(Long id) {
        WarehouseTransaction transaction =
                transactionRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));
        return transactionMapper.toResponse(transaction);
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public List<WarehouseTransactionResponse> getAll() {
        return transactionRepo.findAll().stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public List<WarehouseTransactionResponse> getByWarehouse(Long warehouseId) {
        return transactionRepo.findByWarehouseId(warehouseId).stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public List<WarehouseTransactionResponse> getBySupplier(Long supplierId) {
        return transactionRepo.findBySupplierId(supplierId).stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public List<WarehouseTransactionResponse> getByType(String type) {
        return transactionRepo.findByTransactionType(type).stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public List<WarehouseTransactionResponse> getByStatus(String status) {
        return transactionRepo.findByStatus(status).stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public List<WarehouseTransactionResponse> getByOrderId(String orderId) {
        return transactionRepo.findByOrderId(orderId).stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public List<WarehouseTransactionResponse> getByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepo.findByDateRange(startDate, endDate).stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public WarehouseTransactionResponse cancelTransaction(Long id) {
        log.info("Cancelling transaction with id: {}", id);

        WarehouseTransaction transaction =
                transactionRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));

        if (TransactionStatus.COMPLETED.name().equals(transaction.getStatus())) {
            throw new AppException(ErrorCode.CANNOT_CANCEL_COMPLETED_TRANSACTION);
        }

        if (TransactionStatus.CANCELLED.name().equals(transaction.getStatus())) {
            throw new AppException(ErrorCode.TRANSACTION_ALREADY_CANCELLED);
        }

        transaction.setStatus(TransactionStatus.CANCELLED.name());
        return transactionMapper.toResponse(transactionRepo.save(transaction));
    }

    private void validateTransactionType(String requestType, String expectedType) {
        if (!expectedType.equals(requestType)) {
            throw new AppException(ErrorCode.INVALID_TRANSACTION_TYPE);
        }
    }

    private void verifyStaffExists(Long staffId) {
        try {
            userClient.getStaffById(staffId);
        } catch (FeignException.NotFound e) {
            throw new AppException(ErrorCode.STAFF_NOT_FOUND);
        } catch (FeignException e) {
            log.error("Error communicating with user service", e);
            throw new AppException(ErrorCode.STAFF_SERVICE_ERROR);
        }
    }
}
