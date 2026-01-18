package com.example.file.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.file.dto.response.FileInfo;
import com.example.file.entity.FileMetadata;

@Mapper(componentModel = "spring")
public interface FileMetadataMapper {
    @Mapping(target = "id", source = "name")
    FileMetadata toFileMetadata(FileInfo fileInfo);
}
