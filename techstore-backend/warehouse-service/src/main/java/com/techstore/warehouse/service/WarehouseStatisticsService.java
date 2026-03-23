package com.techstore.warehouse.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.techstore.warehouse.constant.PeriodType;
import com.techstore.warehouse.dto.response.CostStatPoint;
import com.techstore.warehouse.dto.response.InboundCostStatResponse;
import com.techstore.warehouse.repository.WarehouseTransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseStatisticsService {

    private final WarehouseTransactionRepository transactionRepo;

    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_STAFF')")
    public InboundCostStatResponse getInboundCostStats(PeriodType periodType, LocalDateTime from, LocalDateTime to) {

        // Nếu không truyền toDate thì mặc định là hiện tại
        LocalDateTime effectiveTo = to != null ? to : LocalDateTime.now();
        // Nếu không truyền fromDate thì mặc định theo periodType
        LocalDateTime effectiveFrom = from != null ? from : resolveDefaultFrom(periodType, effectiveTo);

        List<Object[]> rawRows =
                switch (periodType) {
                    case TODAY -> transactionRepo.findDailyInboundCost(effectiveFrom, effectiveTo);
                    case MONTHLY -> transactionRepo.findMonthlyInboundCost(effectiveFrom, effectiveTo);
                    case QUARTERLY -> transactionRepo.findQuarterlyInboundCost(effectiveFrom, effectiveTo);
                    case YEARLY -> transactionRepo.findYearlyInboundCost(effectiveFrom, effectiveTo);
                    case CUSTOM -> transactionRepo.findMonthlyInboundCost(
                            effectiveFrom, effectiveTo); // default groupBy month
                };

        List<CostStatPoint> points = rawRows.stream()
                .map(row -> CostStatPoint.builder()
                        .period((String) row[0])
                        .totalCost(((Number) row[1]).longValue())
                        .totalQuantity(((Number) row[2]).longValue())
                        .transactionCount(((Number) row[3]).longValue())
                        .build())
                .toList();

        long grandTotalCost =
                points.stream().mapToLong(CostStatPoint::getTotalCost).sum();
        long grandTotalQty =
                points.stream().mapToLong(CostStatPoint::getTotalQuantity).sum();
        long grandTxCount =
                points.stream().mapToLong(CostStatPoint::getTransactionCount).sum();

        return InboundCostStatResponse.builder()
                .data(points)
                .grandTotalCost(grandTotalCost)
                .grandTotalQuantity(grandTotalQty)
                .grandTransactionCount(grandTxCount)
                .periodType(periodType.name())
                .fromDate(effectiveFrom)
                .toDate(effectiveTo)
                .build();
    }

    /** Mặc định khoảng thời gian nếu FE không truyền from */
    private LocalDateTime resolveDefaultFrom(PeriodType type, LocalDateTime to) {
        return switch (type) {
            case TODAY -> to.toLocalDate().atStartOfDay();
            case MONTHLY -> to.minusMonths(11).withDayOfMonth(1).toLocalDate().atStartOfDay();
            case QUARTERLY -> to.minusMonths(11).withDayOfMonth(1).toLocalDate().atStartOfDay();
            case YEARLY -> to.minusYears(4).withDayOfYear(1).toLocalDate().atStartOfDay();
            case CUSTOM -> to.minusMonths(1).toLocalDate().atStartOfDay();
        };
    }
}
