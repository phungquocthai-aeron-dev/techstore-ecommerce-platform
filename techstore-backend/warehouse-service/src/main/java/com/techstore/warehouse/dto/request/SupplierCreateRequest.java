package com.techstore.warehouse.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierCreateRequest {

    @NotBlank(message = "Supplier name is required")
    private String name;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^(03|05|07|08|09)[0-9]{8}$", message = "Invalid phone number")
    private String phone;
}
