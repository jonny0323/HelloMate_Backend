package com.HelloMate.HelloMateBackend.domain.community.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ReportRequest(@NotBlank String reason) {
}
