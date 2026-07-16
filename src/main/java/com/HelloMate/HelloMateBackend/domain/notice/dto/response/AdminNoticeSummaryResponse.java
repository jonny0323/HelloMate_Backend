package com.HelloMate.HelloMateBackend.domain.notice.dto.response;

import com.HelloMate.HelloMateBackend.domain.notice.entity.Notice;
import com.HelloMate.HelloMateBackend.domain.notice.entity.NoticeType;

import java.time.LocalDateTime;

public record AdminNoticeSummaryResponse(
        String id,
        String title,
        String department,
        NoticeType type,
        int totalRecipientCount,
        long readCount,
        double readRate,
        LocalDateTime createdAt
) {
    public static AdminNoticeSummaryResponse of(Notice notice, long readCount) {
        double readRate = notice.getTotalRecipientCount() == 0
                ? 0.0
                : (double) readCount / notice.getTotalRecipientCount();
        return new AdminNoticeSummaryResponse(
                notice.getId(),
                notice.getTitle(),
                notice.getDepartment(),
                notice.getType(),
                notice.getTotalRecipientCount(),
                readCount,
                readRate,
                notice.getCreatedAt()
        );
    }
}
