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
    @Mapping(target = "variantInfo", ignore = true)
    InventoryResponse toResponse(Inventory inventory);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "warehouse", ignore = true)
    @Mapping(target = "variantId", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(InventoryUpdateRequest request, @MappingTarget Inventory inventory);
}
