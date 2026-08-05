package com.HelloMate.HelloMateBackend.domain.verification.dto.request;

import com.HelloMate.HelloMateBackend.domain.verification.entity.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubmitVerificationDocumentRequest(@NotBlank String fileId, @NotNull DocumentType documentType) {
}
