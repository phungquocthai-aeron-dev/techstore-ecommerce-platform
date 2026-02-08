package com.techstore.warehouse.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.techstore.warehouse.dto.response.TransactionDetailResponse;
import com.techstore.warehouse.dto.response.WarehouseTransactionResponse;
import com.techstore.warehouse.entity.WarehouseTransaction;
import com.techstore.warehouse.entity.WarehouseTransactionDetail;

@Mapper(
        componentModel = "spring",
        uses = {WarehouseMapper.class, SupplierMapper.class, InventoryMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface WarehouseTransactionMapper {
    WarehouseTransactionResponse toResponse(WarehouseTransaction transaction);

    TransactionDetailResponse toDetailResponse(WarehouseTransactionDetail detail);
}
