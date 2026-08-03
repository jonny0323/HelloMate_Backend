package com.HelloMate.HelloMateBackend.domain.verification.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SubmitVerificationDocumentRequest(@NotBlank String fileId) {
}
