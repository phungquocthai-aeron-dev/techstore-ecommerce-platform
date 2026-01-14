package com.techstore.identity.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.techstore.identity.dto.request.RoleRequest;
import com.techstore.identity.dto.response.RoleResponse;
import com.techstore.identity.entity.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    Role toEntity(RoleRequest request);

    RoleResponse toResponse(Role role);

    List<RoleResponse> toResponseList(List<Role> roles);
}

