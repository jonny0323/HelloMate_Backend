package com.HelloMate.HelloMateBackend.domain.community.dto.response;

import com.HelloMate.HelloMateBackend.domain.community.entity.PostReport;
import com.HelloMate.HelloMateBackend.domain.community.entity.ReportStatus;

import java.time.LocalDateTime;

public record FlaggedPostResponse(
        String reportId,
        String postId,
        String postTitle,
        String reason,
        ReportStatus status,
        LocalDateTime reportedAt
) {
    public static FlaggedPostResponse from(PostReport report) {
        return new FlaggedPostResponse(
                report.getId(),
                report.getPost().getId(),
                report.getPost().getTitle(),
                report.getReason(),
                report.getStatus(),
                report.getCreatedAt()
        );
    }
}
