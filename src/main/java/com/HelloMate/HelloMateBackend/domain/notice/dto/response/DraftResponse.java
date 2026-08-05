package com.HelloMate.HelloMateBackend.domain.notice.dto.response;

import com.HelloMate.HelloMateBackend.domain.notice.entity.Notice;
import com.HelloMate.HelloMateBackend.domain.notice.entity.NoticeType;

import java.time.LocalDateTime;

public record DraftResponse(
        String id,
        String title,
        String content,
        NoticeType type,
        LocalDateTime updatedAt
) {
    public static DraftResponse from(Notice notice) {
        return new DraftResponse(notice.getId(), notice.getTitle(), notice.getContent(),
                notice.getType(), notice.getUpdatedAt());
    }
}
