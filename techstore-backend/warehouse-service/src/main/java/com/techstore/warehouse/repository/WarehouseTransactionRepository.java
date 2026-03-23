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

    @Query(
            value =
                    """
			SELECT
				DATE_FORMAT(wt.created_at, '%Y-%m-%d %H:00') AS period,
				COALESCE(SUM(wtd.quantity * wtd.cost), 0) AS totalCost,
				COALESCE(SUM(wtd.quantity), 0) AS totalQuantity,
				COUNT(DISTINCT wt.id) AS transactionCount
			FROM warehouse_transaction wt
			JOIN warehouse_transaction_detail wtd ON wtd.transaction_id = wt.id
			WHERE wt.transaction_type = 'INBOUND'
			AND wt.status = 'COMPLETED'
			AND wt.created_at BETWEEN :from AND :to
			GROUP BY DATE_FORMAT(wt.created_at, '%Y-%m-%d %H:00')
			ORDER BY period ASC
			""",
            nativeQuery = true)
    List<Object[]> findDailyInboundCost(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Thống kê theo THÁNG
    @Query(
            value =
                    """
		SELECT
			DATE_FORMAT(wt.created_at, '%Y-%m') AS period,
			COALESCE(SUM(wtd.quantity * wtd.cost), 0) AS totalCost,
			COALESCE(SUM(wtd.quantity), 0) AS totalQuantity,
			COUNT(DISTINCT wt.id) AS transactionCount
		FROM warehouse_transaction wt
		JOIN warehouse_transaction_detail wtd ON wtd.transaction_id = wt.id
		WHERE wt.transaction_type = 'INBOUND'
		AND wt.status = 'COMPLETED'
		AND wt.created_at BETWEEN :from AND :to
		GROUP BY DATE_FORMAT(wt.created_at, '%Y-%m')
		ORDER BY period ASC
		""",
            nativeQuery = true)
    List<Object[]> findMonthlyInboundCost(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Thống kê theo QUÝ
    @Query(
            value =
                    """
		SELECT
			CONCAT(YEAR(wt.created_at), '-Q', QUARTER(wt.created_at)) AS period,
			COALESCE(SUM(wtd.quantity * wtd.cost), 0) AS totalCost,
			COALESCE(SUM(wtd.quantity), 0) AS totalQuantity,
			COUNT(DISTINCT wt.id) AS transactionCount
		FROM warehouse_transaction wt
		JOIN warehouse_transaction_detail wtd ON wtd.transaction_id = wt.id
		WHERE wt.transaction_type = 'INBOUND'
		AND wt.status = 'COMPLETED'
		AND wt.created_at BETWEEN :from AND :to
		GROUP BY YEAR(wt.created_at), QUARTER(wt.created_at)
		ORDER BY YEAR(wt.created_at), QUARTER(wt.created_at) ASC
		""",
            nativeQuery = true)
    List<Object[]> findQuarterlyInboundCost(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Thống kê theo NĂM
    @Query(
            value =
                    """
		SELECT
			CAST(YEAR(wt.created_at) AS CHAR) AS period,
			COALESCE(SUM(wtd.quantity * wtd.cost), 0) AS totalCost,
			COALESCE(SUM(wtd.quantity), 0) AS totalQuantity,
			COUNT(DISTINCT wt.id) AS transactionCount
		FROM warehouse_transaction wt
		JOIN warehouse_transaction_detail wtd ON wtd.transaction_id = wt.id
		WHERE wt.transaction_type = 'INBOUND'
		AND wt.status = 'COMPLETED'
		AND wt.created_at BETWEEN :from AND :to
		GROUP BY YEAR(wt.created_at)
		ORDER BY YEAR(wt.created_at) ASC
		""",
            nativeQuery = true)
    List<Object[]> findYearlyInboundCost(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
