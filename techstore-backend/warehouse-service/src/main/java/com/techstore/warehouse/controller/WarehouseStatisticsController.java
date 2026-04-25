package com.techstore.warehouse.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.techstore.warehouse.constant.PeriodType;
import com.techstore.warehouse.dto.response.ApiResponse;
import com.techstore.warehouse.dto.response.InboundCostStatResponse;
import com.techstore.warehouse.service.WarehouseStatisticsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class WarehouseStatisticsController {

    private final WarehouseStatisticsService statisticsService;

    /**
     * Thống kê chi phí nhập kho theo khoảng thời gian
     *
     * Query params:
     *   periodType  : MONTHLY | QUARTERLY | YEARLY | CUSTOM  (required)
     *   from        : ISO datetime, e.g. 2024-01-01T00:00:00  (optional — có default)
     *   to          : ISO datetime  (optional — default: now)
     */
    //    @GetMapping("/inbound-cost")
    //    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_STAFF')")
    //    public ApiResponse<InboundCostStatResponse> getInboundCostStats(
    //            @RequestParam PeriodType periodType,
    //            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime
    // from,
    //            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to)
    // {
    //
    //        return ApiResponse.<InboundCostStatResponse>builder()
    //                .result(statisticsService.getInboundCostStats(periodType, from, to))
    //                .build();
    //    }

    @GetMapping("/inbound-cost")
    public ApiResponse<InboundCostStatResponse> getInboundCostStats(
            @RequestParam PeriodType periodType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ApiResponse.<InboundCostStatResponse>builder()
                .result(statisticsService.getInboundCostStats(periodType, from, to))
                .build();
    }
}
