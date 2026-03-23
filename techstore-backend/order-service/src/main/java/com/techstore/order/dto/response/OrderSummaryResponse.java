package com.techstore.order.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderSummaryResponse {
    private long totalOrders;
    private double totalRevenue;
    private List<StatusCount> statusBreakdown;

    @Getter
    @Builder
    public static class StatusCount {
        private String status;
        private long count;
        private double revenue;
    }
}
