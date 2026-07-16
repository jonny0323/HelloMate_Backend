package com.HelloMate.HelloMateBackend.domain.file.controller;

import com.HelloMate.HelloMateBackend.domain.file.dto.request.PresignedUrlRequest;
import com.HelloMate.HelloMateBackend.domain.file.dto.response.PresignedUrlResponse;
import com.HelloMate.HelloMateBackend.domain.file.service.FileStorageService;
import com.HelloMate.HelloMateBackend.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping("/presigned-url")
    public ApiResponse<PresignedUrlResponse> issuePresignedUrl(@Valid @RequestBody PresignedUrlRequest request) {
        return ApiResponse.ok(fileStorageService.issuePresignedUrl(request.filename(), request.contentType(), request.purpose()));
    }
}
