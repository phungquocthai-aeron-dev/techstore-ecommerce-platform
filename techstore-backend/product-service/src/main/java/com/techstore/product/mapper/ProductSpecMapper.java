package com.techstore.product.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.techstore.product.dto.request.ProductSpecRequestDTO;
import com.techstore.product.dto.response.ProductSpecResponseDTO;
import com.techstore.product.entity.ProductSpec;

@Mapper(componentModel = "spring")
public interface ProductSpecMapper {

    ProductSpecResponseDTO toResponseDTO(ProductSpec spec);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    ProductSpec toEntity(ProductSpecRequestDTO dto);
}
