package com.techstore.order.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductSalesResponse {
    private Long productId;
    private String period;
    private long totalQuantitySold;

    private List<VariantSales> variants;

    @Getter
    @Builder
    public static class VariantSales {
        private Long variantId;
        private String variantName;
        private long totalQuantitySold;
        private List<SalesDataPoint> dataPoints;
    }

    @Getter
    @Builder
    public static class SalesDataPoint {
        private String label;
        private long quantitySold;
    }
}
