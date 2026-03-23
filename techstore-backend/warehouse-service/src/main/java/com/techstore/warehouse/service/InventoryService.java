package com.techstore.warehouse.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techstore.warehouse.client.ProductServiceClient;
import com.techstore.warehouse.constant.InventoryStatus;
import com.techstore.warehouse.dto.request.InventoryUpdateRequest;
import com.techstore.warehouse.dto.response.InventoryResponse;
import com.techstore.warehouse.dto.response.VariantInfo;
import com.techstore.warehouse.dto.response.VariantStockResponse;
import com.techstore.warehouse.entity.Inventory;
import com.techstore.warehouse.entity.Warehouse;
import com.techstore.warehouse.exception.AppException;
import com.techstore.warehouse.exception.ErrorCode;
import com.techstore.warehouse.mapper.InventoryMapper;
import com.techstore.warehouse.repository.InventoryRepository;
import com.techstore.warehouse.repository.WarehouseRepository;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepo;
    private final WarehouseRepository warehouseRepo;
    private final InventoryMapper inventoryMapper;
    private final ProductServiceClient productClient;

    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_STAFF')")
    @Transactional
    public InventoryResponse update(Long id, InventoryUpdateRequest req) {
        log.info("Updating inventory with id: {}", id);

        Inventory inventory =
                inventoryRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));

        inventoryMapper.updateEntityFromRequest(req, inventory);

        if (inventory.getStock() < 0) {
            throw new AppException(ErrorCode.INVALID_STOCK_QUANTITY);
        }

        // Auto update status based on stock
        if (inventory.getStock() == 0) {
            inventory.setStatus(InventoryStatus.OUT_OF_STOCK.name());
        } else {
            inventory.setStatus(InventoryStatus.ACTIVE.name());
        }

        Inventory saved = inventoryRepo.save(inventory);
        return buildInventoryResponse(saved);
    }

    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_STAFF','SALES_STAFF')")
    public InventoryResponse getById(Long id) {
        Inventory inventory =
                inventoryRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));
        return buildInventoryResponse(inventory);
    }

    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_STAFF')")
    public List<InventoryResponse> getByWarehouse(Long warehouseId) {
        return buildInventoryResponsesBatch(inventoryRepo.findByWarehouseId(warehouseId));
    }

    public List<InventoryResponse> getByVariant(Long variantId) {
        return buildInventoryResponsesBatch(inventoryRepo.findByVariantId(variantId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_STAFF')")
    public List<InventoryResponse> getByWarehouseAndVariant(Long warehouseId, Long variantId) {
        return buildInventoryResponsesBatch(
                inventoryRepo.findByWarehouseIdAndVariantIdOrderByCreatedAtAsc(warehouseId, variantId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_STAFF')")
    public List<InventoryResponse> getAll() {
        return buildInventoryResponsesBatch(inventoryRepo.findAll());
    }

    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_STAFF')")
    public List<InventoryResponse> getByStatus(String status) {
        return buildInventoryResponsesBatch(inventoryRepo.findByStatus(status));
    }

    public Long getTotalStockByVariant(Long variantId) {
        Long totalStock = inventoryRepo.getTotalStockByVariantId(variantId);
        return totalStock != null ? totalStock : 0L;
    }

    public List<VariantStockResponse> getTotalStockByVariantIds(List<Long> variantIds) {

        List<Object[]> results = inventoryRepo.getTotalStockByVariantIds(variantIds);

        Map<Long, Long> stockMap = results.stream().collect(Collectors.toMap(r -> (Long) r[0], r -> (Long) r[1]));

        // đảm bảo variant không có record vẫn trả về 0
        return variantIds.stream()
                .map(id -> VariantStockResponse.builder()
                        .variantId(id)
                        .stock(stockMap.getOrDefault(id, 0L))
                        .build())
                .toList();
    }

    /**
     * Tạo hoặc cập nhật inventory khi nhập hàng
     */
    @Transactional
    public Inventory createOrUpdateInventory(
            Long warehouseId, Long variantId, Long quantity, Long cost, String batchCode) {
        log.info(
                "Creating or updating inventory for warehouse: {}, variant: {}, quantity: {}",
                warehouseId,
                variantId,
                quantity);

        Warehouse warehouse =
                warehouseRepo.findById(warehouseId).orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));

        if (quantity == null || quantity <= 0) {
            throw new AppException(ErrorCode.INVALID_STOCK_QUANTITY);
        }

        // Verify variant exists
        verifyVariantExists(variantId);

        // Find existing inventory with same batch code
        Inventory inventory = inventoryRepo
                .findByWarehouseIdAndVariantIdAndBatchCode(warehouseId, variantId, batchCode)
                .orElse(null);

        if (inventory != null) {
            // Update existing inventory
            inventory.setStock(inventory.getStock() + quantity);
            if (inventory.getStock() > 0) {
                inventory.setStatus(InventoryStatus.ACTIVE.name());
            }

        } else {
            // Create new inventory
            inventory = Inventory.builder()
                    .warehouse(warehouse)
                    .variantId(variantId)
                    .stock(quantity)
                    .cost(cost)
                    .batchCode(batchCode)
                    .status(InventoryStatus.ACTIVE.name())
                    .build();
        }

        return inventoryRepo.save(inventory);
    }

    /**
     * Giảm số lượng inventory khi xuất hàng
     */
    @Transactional
    public Inventory reduceInventory(Long inventoryId, Long quantity) {
        log.info("Reducing inventory id: {} by quantity: {}", inventoryId, quantity);

        if (quantity == null || quantity <= 0) {
            throw new AppException(ErrorCode.INVALID_STOCK_QUANTITY);
        }

        Inventory inventory = inventoryRepo
                .findByIdForUpdate(inventoryId)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));

        if (inventory.getStock() < quantity) {
            throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
        }

        inventory.setStock(inventory.getStock() - quantity);

        // Update status if out of stock
        if (inventory.getStock() == 0) {
            inventory.setStatus(InventoryStatus.OUT_OF_STOCK.name());
        } else {
            inventory.setStatus(InventoryStatus.ACTIVE.name());
        }

        return inventoryRepo.save(inventory);
    }

    /**
     * Tìm inventory phù hợp để xuất hàng (FIFO - First In First Out)
     */
    @Deprecated
    public Inventory findAvailableInventory(Long warehouseId, Long variantId, Long requiredQuantity) {
        List<Inventory> inventories = inventoryRepo.findByWarehouseAndVariant(warehouseId, variantId);

        return inventories.stream()
                .filter(inv -> InventoryStatus.ACTIVE.name().equals(inv.getStatus()))
                .filter(inv -> inv.getStock() >= requiredQuantity)
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.INSUFFICIENT_STOCK));
    }

    public List<Inventory> findByWarehouseAndVariantForUpdate(Long warehouseId, Long variantId) {
        return inventoryRepo.findByWarehouseAndVariantForUpdate(warehouseId, variantId);
    }

    public List<InventoryResponse> findActiveInventories(Long warehouseId, Long variantId) {
        List<Inventory> inventories =
                inventoryRepo.findByWarehouseIdAndVariantIdOrderByCreatedAtAsc(warehouseId, variantId).stream()
                        .filter(inv -> InventoryStatus.ACTIVE.name().equals(inv.getStatus()))
                        .toList();

        return buildInventoryResponsesBatch(inventories);
    }

    private InventoryResponse buildInventoryResponse(Inventory inventory) {
        InventoryResponse response = inventoryMapper.toResponse(inventory);

        // Fetch variant info from product service
        try {
            VariantInfo variantInfo =
                    productClient.getVariantById(inventory.getVariantId()).getResult();
            response.setVariantInfo(variantInfo);
        } catch (FeignException e) {
            log.error("Error fetching variant info for variantId: {}", inventory.getVariantId(), e);
            // Continue without variant info
        }

        return response;
    }

    private void verifyVariantExists(Long variantId) {
        try {
            productClient.getVariantById(variantId);
        } catch (FeignException.NotFound e) {
            throw new AppException(ErrorCode.VARIANT_NOT_FOUND);
        } catch (FeignException e) {
            log.error("Error communicating with product service", e);
            throw new AppException(ErrorCode.PRODUCT_SERVICE_ERROR);
        }
    }

    private List<InventoryResponse> buildInventoryResponsesBatch(List<Inventory> inventories) {

        if (inventories.isEmpty()) {
            return List.of();
        }

        // 1. Collect unique variantIds
        List<Long> variantIds =
                inventories.stream().map(Inventory::getVariantId).distinct().toList();

        // 2. Call product-service ONE TIME
        List<VariantInfo> variantInfos;
        try {
            variantInfos = productClient.getVariantsByIds(variantIds).getResult();
        } catch (FeignException e) {
            log.error("Error fetching variant batch info", e);
            variantInfos = List.of();
        }

        // 3. Convert to Map for fast lookup
        var variantMap = variantInfos.stream().collect(java.util.stream.Collectors.toMap(VariantInfo::getId, v -> v));

        // 4. Build response
        return inventories.stream()
                .map(inv -> {
                    InventoryResponse response = inventoryMapper.toResponse(inv);
                    response.setVariantInfo(variantMap.get(inv.getVariantId()));
                    return response;
                })
                .toList();
    }
}
