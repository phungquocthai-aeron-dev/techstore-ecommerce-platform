package com.techstore.order.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RevenueStatsResponse {
    private double totalRevenue;
    private long totalOrders;
    private List<RevenueDataPoint> dataPoints;

    @Getter
    @Builder
    public static class RevenueDataPoint {
        private String label;
        private double revenue;
        private long orderCount;
    }
}
