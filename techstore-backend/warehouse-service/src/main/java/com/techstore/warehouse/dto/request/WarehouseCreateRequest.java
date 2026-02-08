package com.techstore.warehouse.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseCreateRequest {

    @NotBlank(message = "Warehouse name is required")
    private String name;

    @NotNull(message = "Max capacity is required")
    private String maxCapacity;

    @NotBlank(message = "Unit capacity is required")
    private String unitCapacity;

    @NotBlank(message = "Address ID is required")
    private String addressId;
}
