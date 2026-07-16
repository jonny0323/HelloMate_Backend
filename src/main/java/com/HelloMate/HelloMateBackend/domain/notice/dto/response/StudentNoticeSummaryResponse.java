package com.HelloMate.HelloMateBackend.domain.notice.dto.response;

import com.HelloMate.HelloMateBackend.domain.notice.entity.NoticeReception;
import com.HelloMate.HelloMateBackend.domain.notice.entity.NoticeType;

import java.time.LocalDateTime;

public record StudentNoticeSummaryResponse(
        String id,
        String title,
        String department,
        NoticeType type,
        boolean isRead,
        LocalDateTime createdAt
) {
    public static StudentNoticeSummaryResponse from(NoticeReception reception) {
        return new StudentNoticeSummaryResponse(
                reception.getNotice().getId(),
                reception.getNotice().getTitle(),
                reception.getNotice().getDepartment(),
                reception.getNotice().getType(),
                reception.isRead(),
                reception.getNotice().getCreatedAt()
        );
    }
}
