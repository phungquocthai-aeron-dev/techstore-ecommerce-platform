package com.techstore.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.techstore.user.dto.request.StaffRequest;
import com.techstore.user.dto.response.StaffResponse;
import com.techstore.user.entity.Staff;

@Mapper(componentModel = "spring")
public interface StaffMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    Staff toEntity(StaffRequest request);

    @Mapping(
            target = "roles",
            expression = "java(staff.getRoles() == null ? null : " + "staff.getRoles().stream()"
                    + ".map(r -> r.getName())"
                    + ".collect(java.util.stream.Collectors.joining(\" \")))")
    StaffResponse toResponse(Staff staff);
}
