package com.HelloMate.HelloMateBackend.domain.notice.dto.response;

import com.HelloMate.HelloMateBackend.domain.notice.entity.NoticeReception;

public record NoticeReceptionResponse(String studentId, String studentName, boolean isRead) {
    public static NoticeReceptionResponse from(NoticeReception reception) {
        return new NoticeReceptionResponse(
                reception.getStudent().getId(),
                reception.getStudent().getName(),
                reception.isRead()
        );
    }
}
