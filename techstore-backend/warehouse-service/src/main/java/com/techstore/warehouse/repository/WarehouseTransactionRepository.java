package com.techstore.warehouse.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techstore.warehouse.entity.WarehouseTransaction;

@Repository
public interface WarehouseTransactionRepository extends JpaRepository<WarehouseTransaction, Long> {

    List<WarehouseTransaction> findByWarehouseId(Long warehouseId);

    List<WarehouseTransaction> findBySupplierId(Long supplierId);

    List<WarehouseTransaction> findByTransactionType(String transactionType);

    List<WarehouseTransaction> findByStatus(String status);

    List<WarehouseTransaction> findByOrderId(Long orderId);

    @Query("SELECT t FROM WarehouseTransaction t WHERE t.createdAt BETWEEN :startDate AND :endDate")
    List<WarehouseTransaction> findByDateRange(
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM WarehouseTransaction t WHERE t.warehouse.id = :warehouseId "
            + "AND t.transactionType = :type AND t.createdAt BETWEEN :startDate AND :endDate")
    List<WarehouseTransaction> findByWarehouseAndTypeAndDateRange(
            @Param("warehouseId") Long warehouseId,
            @Param("type") String type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
