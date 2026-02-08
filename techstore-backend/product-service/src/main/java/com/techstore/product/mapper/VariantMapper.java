package com.techstore.product.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.techstore.product.dto.request.VariantCreateRequestDTO;
import com.techstore.product.dto.response.VariantResponseDTO;
import com.techstore.product.entity.Variant;

@Mapper(componentModel = "spring")
public interface VariantMapper {

    VariantResponseDTO toResponseDTO(Variant variant);

    List<VariantResponseDTO> toResponseDTOList(List<Variant> variants);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    Variant toEntity(VariantCreateRequestDTO dto);
}
