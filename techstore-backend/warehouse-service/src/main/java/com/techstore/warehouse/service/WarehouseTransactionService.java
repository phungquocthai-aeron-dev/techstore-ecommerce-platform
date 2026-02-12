package com.techstore.warehouse.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techstore.warehouse.client.UserServiceClient;
import com.techstore.warehouse.constant.InventoryStatus;
import com.techstore.warehouse.constant.TransactionStatus;
import com.techstore.warehouse.constant.TransactionType;
import com.techstore.warehouse.dto.request.OrderItemRequest;
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
import com.techstore.warehouse.repository.InventoryRepository;
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
    private final InventoryRepository inventoryRepo;

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
                    .cost(detailReq.getCost())
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

            long remaining = detailReq.getQuantity();

            List<Inventory> inventories =
                    inventoryService.findByWarehouseAndVariantForUpdate(warehouse.getId(), detailReq.getVariantId());

            for (Inventory inventory : inventories) {

                if (remaining <= 0) break;

                if (inventory.getStock() <= 0) continue;

                long deduct = Math.min(inventory.getStock(), remaining);

                inventory.setStock(inventory.getStock() - deduct);

                if (inventory.getStock() == 0) {
                    inventory.setStatus(InventoryStatus.OUT_OF_STOCK.name());
                } else {
                    inventory.setStatus(InventoryStatus.ACTIVE.name());
                }

                WarehouseTransactionDetail detail = WarehouseTransactionDetail.builder()
                        .transaction(transaction)
                        .inventory(inventory)
                        .variantId(detailReq.getVariantId())
                        .quantity(deduct)
                        .build();

                details.add(detail);

                remaining -= deduct;
            }

            if (remaining > 0) {
                throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
            }
        }

        transaction.setDetails(details);
        transaction.setStatus(TransactionStatus.COMPLETED.name());

        WarehouseTransaction saved = transactionRepo.save(transaction);
        return transactionMapper.toResponse(saved);
    }

    //    @Transactional
    //    public void reduceInventoryByVariant(
    //            Long variantId,
    //            Long quantity) {
    //
    //        if (quantity == null || quantity <= 0) {
    //            throw new AppException(ErrorCode.INVALID_STOCK_QUANTITY);
    //        }
    //
    //        List<Inventory> inventories =
    //                inventoryRepo.findByVariantIdOrderByCreatedAtAscForUpdate(variantId);
    //
    //        Long totalStock = inventories.stream()
    //                .filter(i -> InventoryStatus.ACTIVE.name().equals(i.getStatus()))
    //                .mapToLong(Inventory::getStock)
    //                .sum();
    //
    //        if (totalStock < quantity) {
    //            throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
    //        }
    //
    //        Long remaining = quantity;
    //
    //        for (Inventory inventory : inventories) {
    //
    //            if (!InventoryStatus.ACTIVE.name().equals(inventory.getStatus())) {
    //                continue;
    //            }
    //
    //            if (inventory.getStock() <= 0) {
    //                inventory.setStatus(InventoryStatus.OUT_OF_STOCK.name());
    //                continue;
    //            }
    //
    //            Long deduct = Math.min(inventory.getStock(), remaining);
    //
    //            inventory.setStock(inventory.getStock() - deduct);
    //
    //            if (inventory.getStock() == 0) {
    //                inventory.setStatus(InventoryStatus.OUT_OF_STOCK.name());
    //            }
    //
    //            remaining -= deduct;
    //
    //            if (remaining == 0) {
    //                break;
    //            }
    //        }
    //
    //        if (remaining > 0) {
    //            throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
    //        }
    //    }

    @Transactional
    public void exportInventory(Long orderId, Long staffId, List<OrderItemRequest> items) {

        List<Long> variantIds =
                items.stream().map(OrderItemRequest::getVariantId).toList();

        List<Inventory> inventories = inventoryRepo.findByVariantIdsForUpdate(variantIds);

        // ===== 1. Validate =====
        for (OrderItemRequest item : items) {

            Long totalStock = inventories.stream()
                    .filter(inv -> inv.getVariantId().equals(item.getVariantId()))
                    .filter(inv -> InventoryStatus.ACTIVE.name().equals(inv.getStatus()))
                    .mapToLong(Inventory::getStock)
                    .sum();

            if (totalStock < item.getQuantity()) {
                throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
            }
        }

        // ===== 2. Group transaction theo warehouse =====
        Map<Long, WarehouseTransaction> transactionMap = new HashMap<Long, WarehouseTransaction>();

        // ===== 3. Deduct =====
        for (OrderItemRequest item : items) {

            Long remaining = item.getQuantity();

            for (Inventory inventory : inventories) {

                if (!inventory.getVariantId().equals(item.getVariantId())) continue;
                if (remaining <= 0) break;
                if (inventory.getStock() <= 0) continue;

                Long deduct = Math.min(inventory.getStock(), remaining);

                inventory.setStock(inventory.getStock() - deduct);

                if (inventory.getStock() == 0) {
                    inventory.setStatus(InventoryStatus.OUT_OF_STOCK.name());
                }

                Long warehouseId = inventory.getWarehouse().getId();

                // ===== Lấy hoặc tạo transaction cho warehouse =====
                WarehouseTransaction transaction = transactionMap.computeIfAbsent(warehouseId, id -> {
                    WarehouseTransaction tx = WarehouseTransaction.builder()
                            .warehouse(inventory.getWarehouse())
                            .note("Auto outbound for order " + orderId)
                            .transactionType(TransactionType.OUTBOUND.name())
                            .referenceType("ORDER")
                            .orderId(orderId)
                            .staffId(staffId)
                            .status(TransactionStatus.COMPLETED.name())
                            .details(new ArrayList<>())
                            .build();
                    return tx;
                });

                // ===== Tạo detail =====
                WarehouseTransactionDetail detail = WarehouseTransactionDetail.builder()
                        .transaction(transaction)
                        .inventory(inventory)
                        .variantId(item.getVariantId())
                        .quantity(deduct)
                        .build();

                transaction.getDetails().add(detail);

                remaining -= deduct;
            }
        }

        // ===== 4. Save tất cả transaction 1 lần =====
        transactionRepo.saveAll(transactionMap.values());
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
    public List<WarehouseTransactionResponse> getByOrderId(Long orderId) {
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

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();

        Long authenticatedStaffId = Long.valueOf(jwt.getSubject());
        String userType = jwt.getClaim("user_type");

        // Kiểm tra đúng loại user
        if (!"STAFF".equals(userType)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // So sánh ID trong token với ID truyền vào
        if (!authenticatedStaffId.equals(staffId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

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
