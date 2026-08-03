package com.HelloMate.HelloMateBackend.domain.notice.dto.response;

import com.HelloMate.HelloMateBackend.domain.notice.entity.Notice;
import com.HelloMate.HelloMateBackend.domain.notice.entity.NoticeType;

import java.time.LocalDateTime;
import java.util.List;

public record AdminNoticeDetailResponse(
        String id,
        String title,
        String content,
        String department,
        NoticeType type,
        long readCount,
        int totalRecipientCount,
        double readRate,
        List<NoticeFileResponse> files,
        LocalDateTime createdAt
) {
    public static AdminNoticeDetailResponse of(Notice notice, long readCount, List<NoticeFileResponse> files) {
        double readRate = notice.getTotalRecipientCount() == 0
                ? 0.0
                : (double) readCount / notice.getTotalRecipientCount();
        return new AdminNoticeDetailResponse(
                notice.getId(),
                notice.getTitle(),
                notice.getContent(),
                notice.getDepartment(),
                notice.getType(),
                readCount,
                notice.getTotalRecipientCount(),
                readRate,
                files,
                notice.getCreatedAt()
        );
    }
}
