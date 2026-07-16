package com.HelloMate.HelloMateBackend.domain.verification.dto.request;

import com.HelloMate.HelloMateBackend.domain.verification.entity.VerificationStatus;
import jakarta.validation.constraints.NotNull;

public record ReviewVerificationDocumentRequest(@NotNull VerificationStatus status) {
}
