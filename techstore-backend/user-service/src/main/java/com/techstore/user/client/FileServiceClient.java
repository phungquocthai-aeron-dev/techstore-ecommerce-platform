package com.techstore.user.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.techstore.user.configuration.FileFeignConfig;
import com.techstore.user.dto.response.ApiResponse;
import com.techstore.user.dto.response.FileResponse;

@FeignClient(name = "file-service", url = "${app.services.file}", configuration = FileFeignConfig.class)
public interface FileServiceClient {

    @PostMapping(value = "/media/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<FileResponse> upload(@RequestPart MultipartFile file, @RequestPart("folder") String folder);
}
