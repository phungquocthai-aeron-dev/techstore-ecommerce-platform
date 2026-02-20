package com.techstore.order.mapper;

import org.mapstruct.*;

import com.techstore.order.dto.request.AddressRequest;
import com.techstore.order.dto.response.AddressResponse;
import com.techstore.order.entity.Address;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    Address toEntity(AddressRequest request);

    AddressResponse toResponse(Address address);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(AddressRequest request, @MappingTarget Address address);
}
