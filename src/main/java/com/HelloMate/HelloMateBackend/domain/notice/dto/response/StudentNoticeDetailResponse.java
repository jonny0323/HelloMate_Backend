package com.HelloMate.HelloMateBackend.domain.notice.dto.response;

import com.HelloMate.HelloMateBackend.domain.notice.entity.Notice;
import com.HelloMate.HelloMateBackend.domain.notice.entity.NoticeType;
import com.HelloMate.HelloMateBackend.domain.translation.dto.response.TranslatedContent;

import java.time.LocalDateTime;
import java.util.List;

public record StudentNoticeDetailResponse(
        String id,
        String title,
        String content,
        TranslatedContent translated,
        String department,
        NoticeType type,
        boolean isRead,
        List<NoticeFileResponse> files,
        LocalDateTime createdAt
) {
    public static StudentNoticeDetailResponse of(Notice notice, boolean isRead, List<NoticeFileResponse> files,
                                                  TranslatedContent translated) {
        return new StudentNoticeDetailResponse(
                notice.getId(),
                notice.getTitle(),
                notice.getContent(),
                translated,
                notice.getDepartment(),
                notice.getType(),
                isRead,
                files,
                notice.getCreatedAt()
        );
    }
}
