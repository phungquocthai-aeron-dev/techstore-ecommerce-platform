package com.techstore.product.mapper;

import org.mapstruct.Mapper;

import com.techstore.product.dto.response.BrandResponseDTO;
import com.techstore.product.entity.Brand;

@Mapper(componentModel = "spring")
public interface BrandMapper {

    BrandResponseDTO toResponseDTO(Brand brand);
}
