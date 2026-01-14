package com.techstore.identity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.techstore.identity.dto.request.StaffRequest;
import com.techstore.identity.dto.response.StaffResponse;
import com.techstore.identity.entity.Staff;

@Mapper(componentModel = "spring", uses = RoleMapper.class)
public abstract class StaffMapper {

    @Mapping(target = "roles", source = "roleIds")
    public abstract Staff toEntity(StaffRequest request);

    public abstract StaffResponse toResponse(Staff staff);

}

