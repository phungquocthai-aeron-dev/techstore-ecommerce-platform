package com.techstore.identity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.techstore.identity.dto.response.AuthenticationResponse;
import com.techstore.identity.entity.InvalidatedToken;

@Mapper(componentModel = "spring")
public interface InvalidatedTokenMapper {

    @Mapping(target = "expiredAt", ignore = true)
    @Mapping(target = "revoked", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    InvalidatedToken toEntity(String token);

    AuthenticationResponse toResponse(InvalidatedToken invalidatedToken);
}
