package com.example.file.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.file.entity.FileMetadata;

@Repository
public interface FileMetadataRepository extends MongoRepository<FileMetadata, String> {}
