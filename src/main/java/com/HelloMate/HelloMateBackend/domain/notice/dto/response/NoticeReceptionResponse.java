package com.HelloMate.HelloMateBackend.domain.notice.dto.response;

import com.HelloMate.HelloMateBackend.domain.notice.entity.NoticeReception;

import java.time.LocalDateTime;

public record NoticeReceptionResponse(String studentId, String studentName, String country,
                                       boolean isRead, LocalDateTime readAt) {
    public static NoticeReceptionResponse from(NoticeReception reception) {
        return new NoticeReceptionResponse(
                reception.getStudent().getId(),
                reception.getStudent().getName(),
                reception.getStudent().getCountry(),
                reception.isRead(),
                reception.getReadAt()
        );
    }
}
