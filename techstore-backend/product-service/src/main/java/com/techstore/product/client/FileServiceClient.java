package com.techstore.product.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.techstore.product.configuration.FileFeignConfig;
import com.techstore.product.dto.response.ApiResponse;
import com.techstore.product.dto.response.FileResponse;

@FeignClient(name = "file-service", url = "${app.services.file}", configuration = FileFeignConfig.class)
public interface FileServiceClient {

    @PostMapping(value = "/media/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<FileResponse> upload(@RequestPart MultipartFile file, @RequestPart("folder") String folder);

    @PostMapping(value = "/media/upload-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<List<FileResponse>> uploadMultiple(
            @RequestPart("files") MultipartFile[] files, @RequestPart("folder") String folder);
}
