package com.HelloMate.HelloMateBackend.domain.file.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PresignedUrlRequest(
        @NotBlank String filename,
        @NotBlank String contentType,
        @NotBlank String purpose
) {
}
