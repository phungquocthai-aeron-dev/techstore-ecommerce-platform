package com.techstore.warehouse.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.techstore.warehouse.dto.request.InventoryUpdateRequest;
import com.techstore.warehouse.dto.response.InventoryResponse;
import com.techstore.warehouse.entity.Inventory;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface InventoryMapper {

    @Mapping(source = "warehouse.id", target = "warehouseId")
    @Mapping(source = "warehouse.name", target = "warehouseName")
    InventoryResponse toResponse(Inventory inventory);

    void updateEntityFromRequest(InventoryUpdateRequest request, @MappingTarget Inventory inventory);
}
