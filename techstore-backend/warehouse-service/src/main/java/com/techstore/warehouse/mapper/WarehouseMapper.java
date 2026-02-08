package com.techstore.warehouse.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.techstore.warehouse.dto.request.WarehouseCreateRequest;
import com.techstore.warehouse.dto.request.WarehouseUpdateRequest;
import com.techstore.warehouse.dto.response.WarehouseResponse;
import com.techstore.warehouse.entity.Warehouse;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface WarehouseMapper {
    Warehouse toEntity(WarehouseCreateRequest request);

    WarehouseResponse toResponse(Warehouse warehouse);

    void updateEntityFromRequest(WarehouseUpdateRequest request, @MappingTarget Warehouse warehouse);
}
