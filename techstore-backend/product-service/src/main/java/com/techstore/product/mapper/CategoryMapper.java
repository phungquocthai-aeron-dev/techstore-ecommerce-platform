package com.techstore.product.mapper;

import org.mapstruct.Mapper;

import com.techstore.product.dto.response.CategoryResponseDTO;
import com.techstore.product.entity.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponseDTO toResponseDTO(Category category);
}
