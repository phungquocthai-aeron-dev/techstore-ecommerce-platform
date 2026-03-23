package com.techstore.warehouse.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techstore.warehouse.entity.WarehouseTransactionDetail;

@Repository
public interface WarehouseTransactionDetailRepository extends JpaRepository<WarehouseTransactionDetail, Long> {
    List<WarehouseTransactionDetail> findByTransactionId(Long transactionId);

    List<WarehouseTransactionDetail> findByVariantId(Long variantId);

    List<WarehouseTransactionDetail> findByInventoryId(Long inventoryId);

    Optional<WarehouseTransactionDetail> findByBatchCode(String batchCode);
}
