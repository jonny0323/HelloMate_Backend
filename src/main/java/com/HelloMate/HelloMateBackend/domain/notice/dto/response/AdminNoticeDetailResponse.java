package com.HelloMate.HelloMateBackend.domain.notice.dto.response;

import com.HelloMate.HelloMateBackend.domain.notice.entity.Notice;
import com.HelloMate.HelloMateBackend.domain.notice.entity.NoticeStatus;
import com.HelloMate.HelloMateBackend.domain.notice.entity.NoticeType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record AdminNoticeDetailResponse(
        String id,
        String title,
        String content,
        String department,
        NoticeType type,
        NoticeStatus status,
        String audienceLabel,
        long readCount,
        int totalRecipientCount,
        double readRate,
        int resendCount,
        LocalDateTime lastResentAt,
        LocalDate bannerStartDate,
        LocalDate bannerEndDate,
        boolean canManage,
        List<NoticeFileResponse> files,
        LocalDateTime sentAt
) {
    public static AdminNoticeDetailResponse of(Notice notice, long readCount, boolean canManage,
                                                List<NoticeFileResponse> files) {
        double readRate = notice.getTotalRecipientCount() == 0
                ? 0.0
                : (double) readCount / notice.getTotalRecipientCount();
        return new AdminNoticeDetailResponse(
                notice.getId(),
                notice.getTitle(),
                notice.getContent(),
                notice.getDepartment(),
                notice.getType(),
                notice.getStatus(),
                notice.getAudienceLabel(),
                readCount,
                notice.getTotalRecipientCount(),
                readRate,
                notice.getResendCount(),
                notice.getLastResentAt(),
                notice.getBannerStartDate(),
                notice.getBannerEndDate(),
                canManage,
                files,
                notice.getSentAt()
        );
    }
}
