package com.example.file.entity;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "file_metadata")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileMetadata {
    @MongoId
    String id;

    String ownerId;
    String contentType;
    long size;
    String md5Checksum;
    String path;
}
