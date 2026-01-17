package com.techstore.user.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.techstore.user.dto.request.RoleRequest;
import com.techstore.user.dto.response.RoleResponse;
import com.techstore.user.entity.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "staffs", ignore = true)
    Role toEntity(RoleRequest request);

    RoleResponse toResponse(Role role);

    List<RoleResponse> toResponseList(List<Role> roles);
}
