package com.example.file.service;

import java.io.IOException;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.file.dto.response.FileData;
import com.example.file.dto.response.FileInfo;
import com.example.file.dto.response.FileResponse;
import com.example.file.entity.FileMetadata;
import com.example.file.exception.AppException;
import com.example.file.exception.ErrorCode;
import com.example.file.mapper.FileMetadataMapper;
import com.example.file.repository.FileMetadataRepository;
import com.example.file.repository.FileRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileService {
    FileRepository fileRepository;
    FileMetadataRepository fileMetadataRepository;

    FileMetadataMapper fileMetadataMapper;

    public FileResponse uploadFile(MultipartFile file) throws IOException {
        FileInfo fileInfo = fileRepository.store(file);

        FileMetadata fileMetadata = fileMetadataMapper.toFileMetadata(fileInfo);
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        fileMetadata.setOwnerId(userId);

        fileMetadata = fileMetadataRepository.save(fileMetadata);

        return FileResponse.builder()
                .originalFileName(file.getOriginalFilename())
                .url(fileInfo.getUrl())
                .build();
    }

    public FileData download(String fileName) throws IOException {
        var FileMetadata =
                fileMetadataRepository.findById(fileName).orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));

        var resource = fileRepository.read(FileMetadata);

        return new FileData(FileMetadata.getContentType(), resource);
    }
}
