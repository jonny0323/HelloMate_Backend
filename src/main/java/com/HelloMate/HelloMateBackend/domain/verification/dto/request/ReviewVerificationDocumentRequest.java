package com.HelloMate.HelloMateBackend.domain.verification.dto.request;

import com.HelloMate.HelloMateBackend.domain.verification.entity.VerificationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** 반려(REJECTED)일 때 rejectReason은 필수다 — 검증은 서비스에서 한다. */
public record ReviewVerificationDocumentRequest(@NotNull VerificationStatus status,
                                                 @Size(max = 500) String rejectReason) {
}
