package com.techstore.product.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.techstore.product.dto.request.ProductImageRequestDTO;
import com.techstore.product.dto.response.ProductImageResponseDTO;
import com.techstore.product.entity.ProductImage;

@Mapper(componentModel = "spring")
public interface ProductImageMapper {

    ProductImageResponseDTO toResponseDTO(ProductImage image);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    ProductImage toEntity(ProductImageRequestDTO dto);
}
