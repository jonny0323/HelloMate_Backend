package com.HelloMate.HelloMateBackend.domain.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePostRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 5000) String content
) {
}
