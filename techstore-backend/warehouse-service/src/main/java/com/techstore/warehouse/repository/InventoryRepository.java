package com.techstore.warehouse.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techstore.warehouse.entity.Inventory;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    List<Inventory> findByWarehouseId(Long warehouseId);

    List<Inventory> findByVariantId(Long variantId);

    Optional<Inventory> findByWarehouseIdAndVariantIdAndBatchCode(Long warehouseId, Long variantId, String batchCode);

    List<Inventory> findByStatus(String status);

    @Query("SELECT i FROM Inventory i WHERE i.warehouse.id = :warehouseId AND i.variantId = :variantId")
    List<Inventory> findByWarehouseAndVariant(
            @Param("warehouseId") Long warehouseId, @Param("variantId") Long variantId);

    @Query("SELECT SUM(i.stock) FROM Inventory i WHERE i.variantId = :variantId AND i.status = 'ACTIVE'")
    Long getTotalStockByVariantId(@Param("variantId") Long variantId);
}
