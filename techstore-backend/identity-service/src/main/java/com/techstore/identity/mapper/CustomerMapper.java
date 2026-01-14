package com.techstore.identity.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.techstore.identity.dto.request.CustomerRegisterRequest;
import com.techstore.identity.dto.request.CustomerUpdateRequest;
import com.techstore.identity.dto.response.CustomerResponse;
import com.techstore.identity.entity.Customer;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "provider", constant = "LOCAL")
    @Mapping(target = "providerId", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    Customer toEntity(CustomerRegisterRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(CustomerUpdateRequest request,
                                 @MappingTarget Customer customer);

    CustomerResponse toResponse(Customer customer);
}

