package com.example.file.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.file.constant.UploadFolder;
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

    public FileResponse uploadFile(MultipartFile file, String folder) throws IOException {

        UploadFolder uploadFolder = parseFolder(folder);

        FileInfo fileInfo = fileRepository.store(file, uploadFolder);

        FileMetadata fileMetadata = fileMetadataMapper.toFileMetadata(fileInfo);
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        fileMetadata.setOwnerId(userId);

        fileMetadataRepository.save(fileMetadata);

        return FileResponse.builder()
                .originalFileName(file.getOriginalFilename())
                .url(fileInfo.getUrl())
                .build();
    }

    public List<FileResponse> uploadFiles(MultipartFile[] files, String folder) throws IOException {

        if (files == null || files.length == 0) {
            throw new AppException(ErrorCode.FILE_EMPTY);
        }

        if (files.length > 10) {
            throw new AppException(ErrorCode.FILE_LIMIT_EXCEEDED);
        }

        UploadFolder uploadFolder = parseFolder(folder);

        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        List<FileResponse> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            FileInfo fileInfo = fileRepository.store(file, uploadFolder);

            FileMetadata fileMetadata = fileMetadataMapper.toFileMetadata(fileInfo);
            fileMetadata.setOwnerId(userId);

            fileMetadataRepository.save(fileMetadata);

            responses.add(FileResponse.builder()
                    .originalFileName(file.getOriginalFilename())
                    .url(fileInfo.getUrl())
                    .build());
        }

        return responses;
    }

    public FileData download(String fileName) throws IOException {
        var FileMetadata =
                fileMetadataRepository.findById(fileName).orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));

        var resource = fileRepository.read(FileMetadata);

        return new FileData(FileMetadata.getContentType(), resource);
    }

    private UploadFolder parseFolder(String folder) {
        try {
            return UploadFolder.valueOf(folder);
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_IMAGE_FOLDER);
        }
    }
}
