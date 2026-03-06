package com.example.file.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.file.constant.UploadFolder;
import com.example.file.dto.response.FileInfo;
import com.example.file.entity.FileMetadata;

@Repository
public class FileRepository {
    @Value("${app.file.storage-dir}")
    String storageDir;

    public FileInfo store(MultipartFile file, UploadFolder folder) throws IOException {

        Path baseDir = Paths.get(storageDir, folder.getRelativePath());
        Files.createDirectories(baseDir);

        String fileExtension = StringUtils.getFilenameExtension(file.getOriginalFilename());

        String fileName =
                Objects.isNull(fileExtension) ? UUID.randomUUID().toString() : UUID.randomUUID() + "." + fileExtension;

        Path filePath = baseDir.resolve(fileName).normalize().toAbsolutePath();

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return FileInfo.builder()
                .name(folder.getRelativePath() + "/" + fileName)
                .size(file.getSize())
                .contentType(file.getContentType())
                .md5Checksum(DigestUtils.md5DigestAsHex(file.getInputStream()))
                .path(filePath.toString())
                .url(folder.getRelativePath() + "/" + fileName)
                .build();
    }

    public Resource read(FileMetadata fileMetadata) throws IOException {
        var data = Files.readAllBytes(Path.of(fileMetadata.getPath()));
        return new ByteArrayResource(data);
    }
}
