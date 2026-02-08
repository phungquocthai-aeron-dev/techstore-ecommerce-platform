package com.techstore.warehouse.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseUpdateRequest {
    private String name;
    private String maxCapacity;
    private String unitCapacity;
    private String status;
    private String addressId;
}
