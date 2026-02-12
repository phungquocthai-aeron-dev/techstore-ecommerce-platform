package com.techstore.warehouse.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.techstore.warehouse.dto.request.SupplierCreateRequest;
import com.techstore.warehouse.dto.request.SupplierUpdateRequest;
import com.techstore.warehouse.dto.response.SupplierResponse;
import com.techstore.warehouse.entity.Supplier;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SupplierMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    Supplier toEntity(SupplierCreateRequest request);

    SupplierResponse toResponse(Supplier supplier);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(SupplierUpdateRequest request, @MappingTarget Supplier supplier);
}
