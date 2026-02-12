package com.techstore.warehouse.repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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

    List<Inventory> findByWarehouseIdAndVariantIdOrderByCreatedAtAsc(Long warehouseId, Long variantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
		SELECT i FROM Inventory i
		WHERE i.variantId = :variantId
		ORDER BY i.createdAt ASC
	""")
    List<Inventory> findByVariantIdOrderByCreatedAtAscForUpdate(Long variantId);

    @Query(
            """
			SELECT COALESCE(SUM(i.stock), 0)
			FROM Inventory i
			WHERE i.variantId = :variantId
			AND i.stock > 0
		""")
    Long getTotalStockByVariantId(Long variantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.id = :id")
    Optional<Inventory> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            """
		SELECT i FROM Inventory i
		WHERE i.warehouse.id = :warehouseId
		AND i.variantId = :variantId
		AND i.status = 'ACTIVE'
		ORDER BY i.createdAt ASC
	""")
    List<Inventory> findByWarehouseAndVariantForUpdate(Long warehouseId, Long variantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            """
		SELECT i FROM Inventory i
		WHERE i.variantId IN :variantIds
		ORDER BY i.variantId ASC, i.createdAt ASC
	""")
    List<Inventory> findByVariantIdsForUpdate(@Param("variantIds") List<Long> variantIds);
}
