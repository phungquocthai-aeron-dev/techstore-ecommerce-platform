package com.techstore.warehouse.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techstore.warehouse.client.ProductServiceClient;
import com.techstore.warehouse.constant.InventoryStatus;
import com.techstore.warehouse.dto.request.InventoryUpdateRequest;
import com.techstore.warehouse.dto.response.InventoryResponse;
import com.techstore.warehouse.dto.response.VariantInfo;
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

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @Transactional
    public InventoryResponse update(Long id, InventoryUpdateRequest req) {
        log.info("Updating inventory with id: {}", id);

        Inventory inventory =
                inventoryRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));

        inventoryMapper.updateEntityFromRequest(req, inventory);
        inventory.setUpdatedAt(LocalDate.now());

        // Auto update status based on stock
        if (inventory.getStock() == 0) {
            inventory.setStatus(InventoryStatus.OUT_OF_STOCK.name());
        } else if (req.getStatus() == null) {
            inventory.setStatus(InventoryStatus.ACTIVE.name());
        }

        Inventory saved = inventoryRepo.save(inventory);
        return buildInventoryResponse(saved);
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public InventoryResponse getById(Long id) {
        Inventory inventory =
                inventoryRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));
        return buildInventoryResponse(inventory);
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public List<InventoryResponse> getByWarehouse(Long warehouseId) {
        return inventoryRepo.findByWarehouseId(warehouseId).stream()
                .map(this::buildInventoryResponse)
                .toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public List<InventoryResponse> getByVariant(Long variantId) {
        return inventoryRepo.findByVariantId(variantId).stream()
                .map(this::buildInventoryResponse)
                .toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public List<InventoryResponse> getByWarehouseAndVariant(Long warehouseId, Long variantId) {
        return inventoryRepo.findByWarehouseAndVariant(warehouseId, variantId).stream()
                .map(this::buildInventoryResponse)
                .toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public List<InventoryResponse> getAll() {
        return inventoryRepo.findAll().stream()
                .map(this::buildInventoryResponse)
                .toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public List<InventoryResponse> getByStatus(String status) {
        return inventoryRepo.findByStatus(status).stream()
                .map(this::buildInventoryResponse)
                .toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public Long getTotalStockByVariant(Long variantId) {
        Long totalStock = inventoryRepo.getTotalStockByVariantId(variantId);
        return totalStock != null ? totalStock : 0L;
    }

    /**
     * Tạo hoặc cập nhật inventory khi nhập hàng
     */
    @Transactional
    public Inventory createOrUpdateInventory(Long warehouseId, Long variantId, Long quantity, String batchCode) {
        log.info(
                "Creating or updating inventory for warehouse: {}, variant: {}, quantity: {}",
                warehouseId,
                variantId,
                quantity);

        Warehouse warehouse =
                warehouseRepo.findById(warehouseId).orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));

        // Verify variant exists
        verifyVariantExists(variantId);

        // Find existing inventory with same batch code
        Inventory inventory = inventoryRepo
                .findByWarehouseIdAndVariantIdAndBatchCode(warehouseId, variantId, batchCode)
                .orElse(null);

        if (inventory != null) {
            // Update existing inventory
            inventory.setStock(inventory.getStock() + quantity);
            inventory.setUpdatedAt(LocalDate.now());
            if (inventory.getStock() > 0) {
                inventory.setStatus(InventoryStatus.ACTIVE.name());
            }
        } else {
            // Create new inventory
            inventory = Inventory.builder()
                    .warehouse(warehouse)
                    .variantId(variantId)
                    .stock(quantity)
                    .batchCode(batchCode)
                    .status(InventoryStatus.ACTIVE.name())
                    .updatedAt(LocalDate.now())
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

        Inventory inventory =
                inventoryRepo.findById(inventoryId).orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));

        if (inventory.getStock() < quantity) {
            throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
        }

        inventory.setStock(inventory.getStock() - quantity);
        inventory.setUpdatedAt(LocalDate.now());

        // Update status if out of stock
        if (inventory.getStock() == 0) {
            inventory.setStatus(InventoryStatus.OUT_OF_STOCK.name());
        }

        return inventoryRepo.save(inventory);
    }

    /**
     * Tìm inventory phù hợp để xuất hàng (FIFO - First In First Out)
     */
    public Inventory findAvailableInventory(Long warehouseId, Long variantId, Long requiredQuantity) {
        List<Inventory> inventories = inventoryRepo.findByWarehouseAndVariant(warehouseId, variantId);

        return inventories.stream()
                .filter(inv -> InventoryStatus.ACTIVE.name().equals(inv.getStatus()))
                .filter(inv -> inv.getStock() >= requiredQuantity)
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.INSUFFICIENT_STOCK));
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
}
