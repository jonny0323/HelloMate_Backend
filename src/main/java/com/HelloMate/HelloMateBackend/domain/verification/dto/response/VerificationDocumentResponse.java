package com.HelloMate.HelloMateBackend.domain.verification.dto.response;

import com.HelloMate.HelloMateBackend.domain.verification.entity.VerificationDocument;
import com.HelloMate.HelloMateBackend.domain.verification.entity.VerificationStatus;

import java.time.LocalDateTime;

public record VerificationDocumentResponse(
        String id,
        String studentId,
        String studentName,
        String fileUrl,
        VerificationStatus status,
        LocalDateTime createdAt
) {
    public static VerificationDocumentResponse from(VerificationDocument document) {
        return new VerificationDocumentResponse(
                document.getId(),
                document.getStudent().getId(),
                document.getStudent().getName(),
                document.getFile().getFileUrl(),
                document.getStatus(),
                document.getCreatedAt()
        );
    }
}
