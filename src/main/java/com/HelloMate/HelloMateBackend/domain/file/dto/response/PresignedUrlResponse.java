package com.HelloMate.HelloMateBackend.domain.file.dto.response;

public record PresignedUrlResponse(String uploadUrl, String fileId, String fileUrl) {
}
