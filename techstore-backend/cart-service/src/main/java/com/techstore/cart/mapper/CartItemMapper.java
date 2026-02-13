package com.techstore.cart.mapper;

import java.math.BigDecimal;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.techstore.cart.entity.CartDetail;
import com.techstore.cart.response.CartItemResponse;

@Mapper(componentModel = "spring", imports = BigDecimal.class)
public interface CartItemMapper {

    @Mapping(target = "price", source = "priceSnapshot")
    @Mapping(
            target = "subTotal",
            expression = "java(detail.getPriceSnapshot()" + ".multiply(BigDecimal.valueOf(detail.getQuantity())))")
    CartItemResponse toResponse(CartDetail detail);
}
