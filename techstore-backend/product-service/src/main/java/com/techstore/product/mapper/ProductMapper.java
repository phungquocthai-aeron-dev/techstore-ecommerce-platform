package com.techstore.product.mapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.techstore.product.dto.request.ProductCreateRequestDTO;
import com.techstore.product.dto.request.ProductUpdateRequestDTO;
import com.techstore.product.dto.response.ProductListResponseDTO;
import com.techstore.product.dto.response.ProductResponseDTO;
import com.techstore.product.entity.Product;
import com.techstore.product.entity.ProductImage;

@Mapper(
        componentModel = "spring",
        uses = {
            BrandMapper.class,
            CategoryMapper.class,
            ProductImageMapper.class,
            ProductSpecMapper.class,
            VariantMapper.class
        })
public interface ProductMapper {

    ProductResponseDTO toResponseDTO(Product product);

    List<ProductResponseDTO> toResponseDTOList(List<Product> products);

    @Mapping(source = "brand.name", target = "brandName")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(target = "primaryImage", expression = "java(getPrimaryImageUrl(product))")
    ProductListResponseDTO toListResponseDTO(Product product);

    List<ProductListResponseDTO> toListResponseDTOList(List<Product> products);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "specs", ignore = true)
    @Mapping(target = "variants", ignore = true)
    Product toEntity(ProductCreateRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "specs", ignore = true)
    @Mapping(target = "variants", ignore = true)
    void updateEntity(ProductUpdateRequestDTO dto, @MappingTarget Product product);

    /**
     * Lấy URL ảnh chính của sản phẩm
     */
    default String getPrimaryImageUrl(Product product) {

        if (product == null
                || product.getImages() == null
                || product.getImages().isEmpty()) {
            return null;
        }

        return product.getImages().stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                .map(ProductImage::getUrl)
                .findFirst()
                .orElseGet(() -> product.getImages().iterator().next().getUrl());
    }
}
