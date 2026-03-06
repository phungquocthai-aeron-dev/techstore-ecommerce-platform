package com.example.file.controller;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.file.dto.response.ApiResponse;
import com.example.file.dto.response.FileResponse;
import com.example.file.service.FileService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileController {
    FileService fileService;

    @PostMapping("/media/upload")
    ApiResponse<FileResponse> uploadMedia(@RequestParam MultipartFile file, @RequestParam String folder)
            throws IOException {

        return ApiResponse.<FileResponse>builder()
                .result(fileService.uploadFile(file, folder))
                .build();
    }

    @PostMapping("/media/upload-multiple")
    ApiResponse<List<FileResponse>> uploadMultipleMedia(
            @RequestParam MultipartFile[] files, @RequestParam String folder) throws IOException {

        return ApiResponse.<List<FileResponse>>builder()
                .result(fileService.uploadFiles(files, folder))
                .build();
    }

    @GetMapping("/media/download/img/**")
    ResponseEntity<Resource> downloadImg(HttpServletRequest request) throws IOException {

        String requestUri = request.getRequestURI();
        String filePath = requestUri.substring(requestUri.indexOf("/media/download/img/") + "/media/download/img/".length());
        System.out.println("img/" + filePath);
        var fileData = fileService.download("img/" + filePath);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, fileData.contentType())
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000")
                .body(fileData.resource());
    }
    
    @GetMapping("/media/download/media/**")
    ResponseEntity<Resource> downloadMedia(HttpServletRequest request) throws IOException {

        String requestUri = request.getRequestURI();
        String filePath = requestUri.substring(requestUri.indexOf("/media/download/media/") + "/media/download/media/".length());
        System.out.println("media/" + filePath);
        var fileData = fileService.download("media/" + filePath);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, fileData.contentType())
                .body(fileData.resource());
    }
}
